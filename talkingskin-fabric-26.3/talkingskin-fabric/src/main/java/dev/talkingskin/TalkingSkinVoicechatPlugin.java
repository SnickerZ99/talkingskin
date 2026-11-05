package dev.talkingskin;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.client.Minecraft;

/**
 * Wird nur geladen, wenn Simple Voice Chat installiert ist
 * (Entrypoint "voicechat" in fabric.mod.json).
 */
public class TalkingSkinVoicechatPlugin implements VoicechatPlugin {
	@Override
	public String getPluginId() {
		return "talkingskin";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		// Eigenes Mikrofon
		registration.registerEvent(ClientSoundEvent.class, this::onOwnVoice);
		// Stimmen anderer Spieler (positional, Gruppen)
		registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onReceived);
		registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::onReceived);
	}

	private void onOwnVoice(ClientSoundEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		TalkTracker.pushSamples(mc.player.getUUID(), event.getRawAudio());
	}

	private void onReceived(ClientReceiveSoundEvent event) {
		// getId() = UUID des Senders
		TalkTracker.pushSamples(event.getId(), event.getRawAudio());
	}
}
