package dev.talkingskin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Speichert pro Spieler, wie laut er gerade spricht.
 * Die Audio-Threads von Simple Voice Chat schreiben, der Render-Thread liest.
 */
public final class TalkTracker {
	private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
	/** So lange gilt ein Spieler nach dem letzten Audiopaket noch als "spricht". */
	private static final long HOLD_NANOS = 250_000_000L;

	private TalkTracker() {}

	private static final class State {
		volatile float target;
		volatile long lastVoice;
		// nur Render-Thread:
		float level;
		float presence;
		long lastFrame;
	}

	/** Rohe 16-Bit-Samples (Mikrofon oder empfangene Stimme). Leeres Array = Ende der Uebertragung. */
	public static void pushSamples(UUID id, short[] samples) {
		if (id == null || samples == null) return;
		State st = STATES.computeIfAbsent(id, k -> new State());
		if (samples.length == 0) {
			st.target = 0f;
			st.lastVoice = 0L;
			return;
		}
		double sum = 0;
		for (short s : samples) {
			double v = s / 32768.0;
			sum += v * v;
		}
		double rms = Math.sqrt(sum / samples.length);
		float level = rms < 0.004 ? 0f : (float) Math.min(1.0, Math.sqrt(rms) * 2.2);
		st.target = level;
		if (level > 0f) st.lastVoice = System.nanoTime();
		if (STATES.size() > 256) prune();
	}

	public static float getLevel(UUID id) {
		State st = refresh(id);
		return st == null ? 0f : st.level;
	}

	public static float getPresence(UUID id) {
		State st = refresh(id);
		return st == null ? 0f : st.presence;
	}

	private static void prune() {
		long now = System.nanoTime();
		STATES.entrySet().removeIf(e -> now - e.getValue().lastVoice > 60_000_000_000L);
	}

	private static State refresh(UUID id) {
		State st = STATES.get(id);
		if (st == null) return null;
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - st.lastFrame) / 1_000_000_000f);
		st.lastFrame = now;

		boolean active = now - st.lastVoice < HOLD_NANOS;
		float targetLevel = active ? st.target : 0f;
		float k = targetLevel > st.level ? 28f : 12f;
		st.level += (targetLevel - st.level) * Math.min(1f, k * dt);

		float targetPresence = active ? 1f : 0f;
		float kp = targetPresence > st.presence ? 14f : 5f;
		st.presence += (targetPresence - st.presence) * Math.min(1f, kp * dt);
		return st;
	}
}
