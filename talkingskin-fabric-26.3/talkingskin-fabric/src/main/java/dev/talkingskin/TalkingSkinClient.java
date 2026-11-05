package dev.talkingskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public class TalkingSkinClient implements ClientModInitializer {
	public static final String MOD_ID = "talkingskin";

	@Override
	public void onInitializeClient() {
		TalkingSkinConfig.load();

		// Lippen als zusaetzliche Render-Schicht auf jedem Spielermodell (normal und slim)
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				registrationHelper.register(new LipsLayer(avatarRenderer));
			}
		});
	}
}
