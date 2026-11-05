package dev.talkingskin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Einfache JSON-Config: .minecraft/config/talkingskin.json */
public final class TalkingSkinConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("talkingskin");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static TalkingSkinConfig instance = new TalkingSkinConfig();

	/** Mod komplett an/aus. */
	public boolean enabled = true;
	/** Dicke Lippen anzeigen. */
	public boolean lips = true;
	/** Zaehne im offenen Mund anzeigen. */
	public boolean teeth = true;
	/** Kopf- und Armbewegungen beim Sprechen. */
	public boolean gestures = true;
	/** Leichtes Wippen des ganzen Koerpers beim Sprechen. */
	public boolean bodyMovement = true;
	/** Lippen immer zeigen, auch wenn niemand spricht. */
	public boolean alwaysShowLips = false;
	/** Lippen auch am eigenen Modell zeigen (F5 / Dritte-Person-Ansicht). */
	public boolean showOnSelf = true;
	/** Lippendicke (0.5 = flach, 1 = normal, 2 = sehr dick). */
	public float lipThickness = 1.0f;
	/** Staerke der Gesten und Koerperbewegung (0 = keine, 1 = normal, 2 = stark). */
	public float gestureStrength = 1.0f;
	/** Farben als Hex. */
	public String lipColor = "#D2505F";
	public String lipOutlineColor = "#8B1E2D";
	public String mouthColor = "#3A0A14";
	public String teethColor = "#F2EEE4";

	public transient int lipRgb = 0xD2505F;
	public transient int lipOutlineRgb = 0x8B1E2D;
	public transient int mouthRgb = 0x3A0A14;
	public transient int teethRgb = 0xF2EEE4;

	public static TalkingSkinConfig get() {
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("talkingskin.json");
	}

	public static void load() {
		Path p = path();
		if (Files.exists(p)) {
			try (Reader r = Files.newBufferedReader(p)) {
				TalkingSkinConfig c = GSON.fromJson(r, TalkingSkinConfig.class);
				if (c != null) instance = c;
			} catch (Exception e) {
				LOGGER.warn("Konnte talkingskin.json nicht lesen, nutze Standardwerte", e);
			}
		}
		instance.normalize();
		save();
	}

	public static void save() {
		Path p = path();
		try {
			Files.createDirectories(p.getParent());
			Files.writeString(p, GSON.toJson(instance));
		} catch (IOException e) {
			LOGGER.warn("Konnte talkingskin.json nicht speichern", e);
		}
	}

	private void normalize() {
		lipThickness = clamp(lipThickness, 0.3f, 3f);
		gestureStrength = clamp(gestureStrength, 0f, 3f);
		lipRgb = parseColor(lipColor, 0xD2505F);
		lipOutlineRgb = parseColor(lipOutlineColor, 0x8B1E2D);
		mouthRgb = parseColor(mouthColor, 0x3A0A14);
		teethRgb = parseColor(teethColor, 0xF2EEE4);
	}

	private static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

	private static int parseColor(String hex, int fallback) {
		try {
			return Integer.parseInt(hex.replace("#", "").trim(), 16) & 0xFFFFFF;
		} catch (Exception e) {
			return fallback;
		}
	}
}
