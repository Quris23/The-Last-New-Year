package dev.qur1s.legspyglass.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * While a player looks through the leg spyglass, draws a spyglass at their eye, exactly where vanilla puts the one held in
 * the hand ({@code ItemInHandLayer.renderArmWithSpyglass}).
 */
public final class SpyglassHandLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ItemStack STACK = new ItemStack(Items.SPYGLASS);

    public SpyglassHandLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ClientScope.isScoping(player) || player.isInvisible()) return;
        poseStack.pushPose();
        ModelPart head = getParentModel().getHead();
        float saved = head.xRot;
        head.xRot = Mth.clamp(head.xRot, -0.5236F, 1.4835F);
        head.translateAndRotate(poseStack);
        head.xRot = saved;
        CustomHeadLayer.translateToHead(poseStack, false);
        boolean left = player.getMainArm() == HumanoidArm.LEFT;
        poseStack.translate((left ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer()
                .renderItem(player, STACK, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
