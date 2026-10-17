package dev.qur1s.reviveraltar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.qur1s.reviveraltar.block.RitualAltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Shows the head resting on the altar, and a beacon-style beam for a moment when a revival completes. */
public class RitualAltarRenderer implements BlockEntityRenderer<RitualAltarBlockEntity> {
    public RitualAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RitualAltarBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderHead(be, poseStack, buffer, packedLight);
        renderBeam(be, partialTick, poseStack, buffer);
    }

    private void renderHead(RitualAltarBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack head = be.getHeadSlot();
        if (head.isEmpty()) {
            return;
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(0.5, 13.0 / 16.0 + 0.02, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(0.9F, 0.9F, 0.9F);
        itemRenderer.renderStatic(head, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, be.getLevel(), (int) be.getBlockPos().asLong());
        poseStack.popPose();
    }

    private void renderBeam(RitualAltarBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        long beamStart = be.getBeamStartGameTime();
        if (beamStart < 0 || be.getLevel() == null) {
            return;
        }
        long elapsed = be.getLevel().getGameTime() - beamStart;
        if (elapsed < 0 || elapsed >= RitualAltarBlockEntity.BEAM_DURATION) {
            return;
        }

        poseStack.pushPose();
        int color = FastColor.ARGB32.color(255, 170, 70, 220);
        BeaconRenderer.renderBeaconBeam(poseStack, buffer, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F,
                be.getLevel().getGameTime(), 0, 40, color, 0.2F, 0.25F);
        poseStack.popPose();
    }
}
