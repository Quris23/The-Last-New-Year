package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldRenderer;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the Magic Shield as a regular hexagon (same swirl texture and colour as Iron's shield), matching ShieldHexMixin. */
@Mixin(value = ShieldRenderer.class, remap = false)
public abstract class ShieldRendererHexMixin {
    private static final float RADIUS = 1.1f;
    private static final int FULL_LIGHT = 15728880;
    private static final float FRAME_WIDTH = 0.09f;
    /** Light cyan, fully opaque (ARGB). */
    private static final int FRAME_COLOR = 0xFFB8F4FF;

    @Inject(method = "render(Lio/redspace/ironsspellbooks/entity/spells/shield/ShieldEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$drawHexagon(ShieldEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                                          MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        Vec2 swirl = ShieldRenderer.getEnergySwirlOffset(entity, partialTick);
        VertexConsumer vc = buffer.getBuffer(RenderHelper.CustomerRenderType.magicSwirl(
                ((ShieldRenderer) (Object) this).getTextureLocation(entity), swirl.x, swirl.y));
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        int color = RenderHelper.colorf(0.65f, 0.65f, 0.65f);
        PoseStack.Pose pose = poseStack.last();

        float[] hx = new float[6];
        float[] hy = new float[6];
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(60.0 * k);
            hx[k] = (float) (RADIUS * Math.cos(a));
            hy[k] = (float) (RADIUS * Math.sin(a));
        }
        // three quads: centre + three neighbouring corners each; drawn from both sides
        int[][] quads = {{0, 1, 2}, {2, 3, 4}, {4, 5, 0}};
        for (int[] q : quads) {
            float[] xs = {0, hx[q[0]], hx[q[1]], hx[q[2]]};
            float[] ys = {0, hy[q[0]], hy[q[1]], hy[q[2]]};
            for (int side = 0; side < 2; side++) {
                float nz = side == 0 ? 1f : -1f;
                for (int i = 0; i < 4; i++) {
                    int j = side == 0 ? i : 3 - i;
                    vc.addVertex(pose, xs[j], ys[j], 0f)
                            .setColor(color)
                            .setUv(0.5f + xs[j] / (2 * RADIUS), 0.5f - ys[j] / (2 * RADIUS))
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(FULL_LIGHT)
                            .setNormal(pose, 0f, 0f, nz);
                }
            }
        }
        // bright frame around the hexagon (a ring of six trapezoids), seen from both sides
        VertexConsumer frame = buffer.getBuffer(net.minecraft.client.renderer.RenderType.lightning());
        float inner = RADIUS - FRAME_WIDTH;
        for (int k = 0; k < 6; k++) {
            int k2 = (k + 1) % 6;
            float[] fx = {hx[k], hx[k2], hx[k2] * inner / RADIUS, hx[k] * inner / RADIUS};
            float[] fy = {hy[k], hy[k2], hy[k2] * inner / RADIUS, hy[k] * inner / RADIUS};
            for (int side = 0; side < 2; side++) {
                for (int i = 0; i < 4; i++) {
                    int j = side == 0 ? i : 3 - i;
                    frame.addVertex(pose, fx[j], fy[j], side == 0 ? 0.004f : -0.004f).setColor(FRAME_COLOR);
                }
            }
        }
        poseStack.popPose();
        ci.cancel();
    }
}
