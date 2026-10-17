package dev.qur1s.legspyglass.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Draws the slotted spyglass on the outside of the wearer's right thigh (as the 3D in-hand model, standing upright). */
public final class SpyglassCurioRenderer implements ICurioRenderer {
    private static final float OUT = 0.17f;
    private static final float DOWN = 0.20f;
    private static final float SCALE = 0.6f;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
            PoseStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buffer, int packedLight,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity entity = slotContext.entity();
        // While the spyglass is in the hand it is not on the leg.
        if (entity instanceof Player player && ClientScope.isScoping(player)) return;
        if (!(parent.getModel() instanceof HumanoidModel<?> model)) return;
        poseStack.pushPose();
        model.rightLeg.translateAndRotate(poseStack);
        poseStack.translate(-OUT, DOWN, 0.0f);
        poseStack.scale(SCALE, -SCALE, -SCALE);
        // the model is drawn off-centre by its hand display transform; move its middle back to the origin (pixels -> blocks)
        poseStack.translate(0.75f / 16f, 4.5f / 16f, -1.5f / 16f);
        Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                false, poseStack, buffer, entity.level(), packedLight, OverlayTexture.NO_OVERLAY, entity.getId());
        poseStack.popPose();
    }
}
