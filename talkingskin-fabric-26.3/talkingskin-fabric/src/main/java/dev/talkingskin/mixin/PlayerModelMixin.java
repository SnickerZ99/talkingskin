package dev.talkingskin.mixin;

import dev.talkingskin.TalkRenderData;
import dev.talkingskin.TalkingSkinConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bewegt Kopf, Koerper und freie Arme leicht im Rhythmus der Stimme.
 * Laeuft nach dem normalen setupAnim, damit alle anderen Animationen erhalten bleiben.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
	public PlayerModelMixin(ModelPart root) {
		super(root);
	}

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void talkingskin$animate(AvatarRenderState state, CallbackInfo ci) {
		TalkRenderData data = state.getData(TalkRenderData.KEY);
		if (data == null || !data.visible()) return;

		TalkingSkinConfig cfg = TalkingSkinConfig.get();
		if (!cfg.enabled || cfg.gestureStrength <= 0f) return;
		if (!cfg.gestures && !cfg.bodyMovement) return;

		float s = cfg.gestureStrength * data.presence();
		if (s < 0.01f) return;
		float level = data.level();
		float t = state.ageInTicks;
		float amp = 0.25f + 0.75f * level;   // Grundbewegung + lautstaerkeabhaengig

		if (cfg.bodyMovement) {
			// Ganzer Oberkoerper wippt leicht mit (Kopf, Koerper, Arme gemeinsam)
			float bob = (Mth.sin(t * 1.3f) * 0.35f + level * 0.25f) * amp * s;
			this.head.y += bob;
			this.body.y += bob;
			this.rightArm.y += bob;
			this.leftArm.y += bob;
			// Leichtes Schwanken
			this.body.yRot += Mth.sin(t * 0.45f) * 0.05f * amp * s;
			this.body.zRot += Mth.sin(t * 0.37f + 0.8f) * 0.025f * amp * s;
		}

		if (cfg.gestures) {
			// Kopf: Nicken, Drehen, Neigen
			this.head.xRot += (Mth.sin(t * 0.9f) * 0.06f + level * 0.10f) * s;
			this.head.yRot += Mth.sin(t * 0.45f) * 0.08f * amp * s;
			this.head.zRot += Mth.sin(t * 0.37f + 1.3f) * 0.05f * amp * s;

			// Arme gestikulieren, wenn die Hand frei ist
			if (data.rightHandFree()) {
				this.rightArm.xRot -= level * s * (0.25f + 0.30f * (0.5f + 0.5f * Mth.sin(t * 0.85f)));
				this.rightArm.zRot += level * s * 0.10f * Mth.sin(t * 0.70f);
			}
			if (data.leftHandFree()) {
				this.leftArm.xRot -= level * s * (0.20f + 0.30f * (0.5f + 0.5f * Mth.sin(t * 0.85f + 2.1f)));
				this.leftArm.zRot -= level * s * 0.10f * Mth.sin(t * 0.70f + 1.0f);
			}
		}

		// Aeussere Ebenen (Hut, Jacke, Aermel) neu mit den bewegten Teilen synchronisieren
		this.hat.copyFrom(this.head);
		ModelPart root = this.root();
		if (root.hasChild("jacket")) root.getChild("jacket").copyFrom(this.body);
		if (root.hasChild("left_sleeve")) root.getChild("left_sleeve").copyFrom(this.leftArm);
		if (root.hasChild("right_sleeve")) root.getChild("right_sleeve").copyFrom(this.rightArm);
	}
}
