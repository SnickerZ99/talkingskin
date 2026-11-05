package dev.talkingskin;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

/**
 * Daten, die beim "Extract" aus dem Spieler in den AvatarRenderState kopiert werden.
 * Ab 26.x wird nicht mehr direkt aus der Entity gerendert, sondern aus dem RenderState.
 */
public record TalkRenderData(float level, float presence, boolean visible, boolean rightHandFree, boolean leftHandFree) {
	public static final RenderStateDataKey<TalkRenderData> KEY = RenderStateDataKey.create(() -> "talkingskin:talk");
}
