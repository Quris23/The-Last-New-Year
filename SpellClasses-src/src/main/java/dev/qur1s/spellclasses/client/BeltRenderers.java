package dev.qur1s.spellclasses.client;

import com.github.L_Ender.cataclysm.client.model.CMModelLayers;
import com.github.L_Ender.cataclysm.client.model.item.CuriosModel.Belt_Of_Beginner_Model;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Reuses Cataclysm's own Belt of Beginner geometry (same model, already baked by Cataclysm) so our
 * belts look identical in shape, just with a different-colored texture per tier.
 */
public final class BeltRenderers {
    private BeltRenderers() {
    }

    private static final ResourceLocation BRASS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/curiositem/brass_belt.png");
    private static final ResourceLocation BLACK_STEEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/curiositem/black_steel_belt.png");

    public static class Brass implements ICurioRenderer {
        private final Belt_Of_Beginner_Model model =
                new Belt_Of_Beginner_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.BELT_OF_BEGINNER_MODEL));

        public ResourceLocation getCuriosTexture() {
            return BRASS_TEXTURE;
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
                PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int packedLight,
                float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ICurioRenderer.followBodyRotations(slotContext.entity(), new HumanoidModel[] {this.model});
            VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(this.getCuriosTexture()), stack.hasFoil());
            this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    public static class BlackSteel implements ICurioRenderer {
        private final Belt_Of_Beginner_Model model =
                new Belt_Of_Beginner_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.BELT_OF_BEGINNER_MODEL));

        public ResourceLocation getCuriosTexture() {
            return BLACK_STEEL_TEXTURE;
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
                PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int packedLight,
                float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ICurioRenderer.followBodyRotations(slotContext.entity(), new HumanoidModel[] {this.model});
            VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(this.getCuriosTexture()), stack.hasFoil());
            this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
