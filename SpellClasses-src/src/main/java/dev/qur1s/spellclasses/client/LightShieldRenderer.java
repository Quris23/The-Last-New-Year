package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.qur1s.spellclasses.entity.LightShieldEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Draws the Light Shield as a translucent light-yellow sphere (both faces, so it is visible from inside too). */
public class LightShieldRenderer extends EntityRenderer<LightShieldEntity> {
    private static final int RINGS = 18;
    private static final int SEGMENTS = 32;
    private static final float[] COLOR = {1.0f, 0.94f, 0.55f};

    public LightShieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LightShieldEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }

    @Override
    public boolean shouldRender(LightShieldEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(LightShieldEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTick;
        int remaining = LightShieldEntity.DURATION_TICKS - entity.tickCount;
        float fade = Math.min(1f, Math.min(age / 6f, remaining / 20f));
        if (fade <= 0f) return;
        float pulse = 0.85f + 0.15f * (float) Math.sin(age * 0.2f);
        int alpha = (int) (46 * fade * pulse);
        int r = (int) (COLOR[0] * 255), g = (int) (COLOR[1] * 255), b = (int) (COLOR[2] * 255);

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        float radius = LightShieldEntity.RADIUS;
        for (int i = 0; i < RINGS; i++) {
            double lat0 = Math.PI * i / RINGS - Math.PI / 2;
            double lat1 = Math.PI * (i + 1) / RINGS - Math.PI / 2;
            for (int j = 0; j < SEGMENTS; j++) {
                double lon0 = 2 * Math.PI * j / SEGMENTS;
                double lon1 = 2 * Math.PI * (j + 1) / SEGMENTS;
                float[] a0 = point(radius, lat0, lon0), a1 = point(radius, lat0, lon1);
                float[] a2 = point(radius, lat1, lon1), a3 = point(radius, lat1, lon0);
                quad(vc, m, a0, a1, a2, a3, r, g, b, alpha);
                quad(vc, m, a3, a2, a1, a0, r, g, b, alpha);
            }
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static float[] point(float radius, double lat, double lon) {
        return new float[]{
                (float) (radius * Math.cos(lat) * Math.cos(lon)),
                (float) (radius * Math.sin(lat)),
                (float) (radius * Math.cos(lat) * Math.sin(lon))};
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float[] p0, float[] p1, float[] p2, float[] p3,
                             int r, int g, int b, int a) {
        vc.addVertex(m, p0[0], p0[1], p0[2]).setColor(r, g, b, a);
        vc.addVertex(m, p1[0], p1[1], p1[2]).setColor(r, g, b, a);
        vc.addVertex(m, p2[0], p2[1], p2[2]).setColor(r, g, b, a);
        vc.addVertex(m, p3[0], p3[1], p3[2]).setColor(r, g, b, a);
    }
}
