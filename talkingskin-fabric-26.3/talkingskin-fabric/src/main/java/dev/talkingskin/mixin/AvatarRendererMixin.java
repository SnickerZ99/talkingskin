package dev.talkingskin.mixin;

import dev.talkingskin.TalkRenderData;
import dev.talkingskin.TalkTracker;
import dev.talkingskin.TalkingSkinConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * "Extract"-Phase: kopiert, wie laut der Spieler gerade spricht, in den RenderState.
 * Dort lesen es die Lippen-Schicht und das PlayerModelMixin.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("TAIL"))
	private void talkingskin$extract(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
		TalkingSkinConfig cfg = TalkingSkinConfig.get();
		if (!cfg.enabled) return;

		UUID id = avatar.getUUID();
		float presence = cfg.alwaysShowLips ? 1f : TalkTracker.getPresence(id);
		float level = TalkTracker.getLevel(id);

		boolean visible = !avatar.isInvisible() && !avatar.isSpectator();
		if (!cfg.showOnSelf && avatar == Minecraft.getInstance().player) visible = false;

		// Arme nur bewegen, wenn die Hand frei ist und gerade nichts anderes passiert
		Pose pose = avatar.getPose();
		boolean calm = (pose == Pose.STANDING || pose == Pose.CROUCHING) && !avatar.swinging && !avatar.isUsingItem();
		HumanoidArm main = avatar.getMainArm();
		ItemStack rightStack = main == HumanoidArm.RIGHT ? avatar.getMainHandItem() : avatar.getOffhandItem();
		ItemStack leftStack = main == HumanoidArm.LEFT ? avatar.getMainHandItem() : avatar.getOffhandItem();

		state.setData(TalkRenderData.KEY,
				new TalkRenderData(level, presence, visible, calm && rightStack.isEmpty(), calm && leftStack.isEmpty()));
	}
}
