package dev.talkingskin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Zeichnet dicke Cartoon-Lippen mit Zaehnen auf die Vorderseite des Kopfes.
 * Alle Masse sind Modell-Pixel (1 Pixel = 1/16 Block). Kopf: x -4..4, y -8..0, z -4..4.
 * Das Gesicht liegt bei z = -4 (vorne = negatives z).
 */
public class LipsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	/** Weisse 4x4-Textur, die Farbe kommt ueber die Vertex-Farbe. */
	private static final Identifier WHITE = Identifier.fromNamespaceAndPath(TalkingSkinClient.MOD_ID, "textures/entity/lips.png");

	public LipsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
					   AvatarRenderState state, float yRot, float xRot) {
		TalkRenderData data = state.getData(TalkRenderData.KEY);
		if (data == null || !data.visible()) return;

		TalkingSkinConfig cfg = TalkingSkinConfig.get();
		if (!cfg.enabled || !cfg.lips) return;

		final float s = data.presence();
		if (s < 0.03f) return;
		final float level = data.level();

		poseStack.pushPose();
		// Folgt dem Kopf inkl. Drehung, Schleichen und der Wipp-Animation aus dem PlayerModelMixin
		this.getParentModel().getHead().translateAndRotate(poseStack);
		poseStack.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);

		collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(WHITE), (pose, buffer) ->
				drawMouth(pose, buffer, lightCoords, cfg, level, s));

		poseStack.popPose();
	}

	private static void drawMouth(PoseStack.Pose pose, VertexConsumer vb, int light,
								  TalkingSkinConfig cfg, float level, float s) {
		float halfW = 3.0f * s;                       // breiter Mund
		float lipH = 1.3f * s;                        // dicke Lippen
		float depth = 1.6f * cfg.lipThickness * s;    // ragen deutlich aus dem Gesicht (auch ueber Helme)
		float cy = -1.6f;                             // Mundmitte
		float gap = level * 2.4f * s;                 // Mundoeffnung

		float zBack = -3.9f;
		float zFront = zBack - depth;

		float u0 = cy - lipH - gap * 0.2f;            // Oberlippe
		float u1 = cy - gap * 0.2f;
		float l0 = cy + gap * 0.8f;                   // Unterlippe
		float l1 = cy + lipH + gap * 0.8f;

		// Dunkles Mundinneres
		if (gap > 0.05f) {
			float iw = Math.max(0.2f, halfW - 0.3f);
			quad(pose, vb, light, cfg.mouthRgb, 1.0f, 0, 0, -1,
					-iw, u1, zBack - 0.1f, iw, u1, zBack - 0.1f, iw, l0, zBack - 0.1f, -iw, l0, zBack - 0.1f);
		}

		// Zaehne (weisse Bloecke unter der Oberlippe und ueber der Unterlippe)
		if (cfg.teeth && gap > 0.3f) {
			float toothH = Math.min(0.9f, gap * 0.4f);
			int n = 6;
			float x0 = -(halfW - 0.5f);
			float w = (2f * (halfW - 0.5f)) / n;
			float tz0 = zBack - 0.2f;
			float tz1 = zFront + 0.5f;
			for (int i = 0; i < n; i++) {
				float tx0 = x0 + i * w + 0.06f;
				float tx1 = x0 + (i + 1) * w - 0.06f;
				box(pose, vb, light, cfg.teethRgb, tx0, u1, tz0, tx1, u1 + toothH, tz1);
				box(pose, vb, light, cfg.teethRgb, tx0, l0 - toothH, tz0, tx1, l0, tz1);
			}
		}

		// Lippen: Oberlippe etwas breiter, Unterlippe etwas schmaler
		lipBox(pose, vb, light, cfg, -halfW, u0, zBack, halfW, u1, zFront);
		lipBox(pose, vb, light, cfg, -halfW * 0.92f, l0, zBack, halfW * 0.92f, l1, zFront);
	}

	/** Lippe mit dunkler Kontur auf der Vorderseite. */
	private static void lipBox(PoseStack.Pose pose, VertexConsumer vb, int light, TalkingSkinConfig cfg,
							   float x0, float y0, float z0, float x1, float y1, float z1) {
		// Seiten, Oberseite, Unterseite
		quad(pose, vb, light, cfg.lipRgb, 0.95f, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
		quad(pose, vb, light, cfg.lipRgb, 0.70f, 0, 1, 0, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
		quad(pose, vb, light, cfg.lipRgb, 0.85f, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
		quad(pose, vb, light, cfg.lipRgb, 0.85f, 1, 0, 0, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
		// Vorderseite: erst Kontur, dann Innenflaeche leicht davor
		quad(pose, vb, light, cfg.lipOutlineRgb, 1.0f, 0, 0, -1, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1);
		float in = 0.3f;
		float iz = z1 - 0.03f;
		float ix0 = x0 + in, ix1 = x1 - in, iy0 = y0 + in, iy1 = y1 - in;
		if (ix1 > ix0 && iy1 > iy0) {
			quad(pose, vb, light, cfg.lipRgb, 1.0f, 0, 0, -1, ix0, iy1, iz, ix1, iy1, iz, ix1, iy0, iz, ix0, iy0, iz);
		}
	}

	/** Einfacher Quader (ohne Rueckseite). x0<x1, y0<y1 (y nach unten), z0 hinten, z1 vorne. */
	private static void box(PoseStack.Pose pose, VertexConsumer vb, int light, int rgb,
							float x0, float y0, float z0, float x1, float y1, float z1) {
		quad(pose, vb, light, rgb, 1.00f, 0, 0, -1, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1);
		quad(pose, vb, light, rgb, 0.95f, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
		quad(pose, vb, light, rgb, 0.70f, 0, 1, 0, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
		quad(pose, vb, light, rgb, 0.85f, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
		quad(pose, vb, light, rgb, 0.85f, 1, 0, 0, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer vb, int light, int rgb, float shade,
							 float nx, float ny, float nz,
							 float ax, float ay, float az, float bx, float by, float bz,
							 float cx, float cy, float cz, float dx, float dy, float dz) {
		int r = (int) (((rgb >> 16) & 0xFF) * shade);
		int g = (int) (((rgb >> 8) & 0xFF) * shade);
		int b = (int) ((rgb & 0xFF) * shade);
		vertex(pose, vb, light, r, g, b, nx, ny, nz, ax, ay, az);
		vertex(pose, vb, light, r, g, b, nx, ny, nz, bx, by, bz);
		vertex(pose, vb, light, r, g, b, nx, ny, nz, cx, cy, cz);
		vertex(pose, vb, light, r, g, b, nx, ny, nz, dx, dy, dz);
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer vb, int light,
							   int r, int g, int b, float nx, float ny, float nz,
							   float x, float y, float z) {
		vb.addVertex(pose, x, y, z)
				.setColor(r, g, b, 255)
				.setUv(0.5f, 0.5f)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
	}
}
