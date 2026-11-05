package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Draws Somake's Copper Glove on the wearer's hand when it sits in the hands slot, placed exactly where an item held
 * in that hand would be, so it looks the same as held and turns with the hand it is on (slot 0 right, slot 1 left).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CopperGloveRenderer implements ICurioRenderer {
    /** Every glove that lives in a hands slot: Somake's Copper Glove, the five gauntlets and the Fiery Jaw. */
    private static final java.util.List<ResourceLocation> GLOVES = java.util.List.of(
            ResourceLocation.fromNamespaceAndPath("somakespells", "copper_glove"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_guard"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_bulwark"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_maelstrom"),
            ResourceLocation.fromNamespaceAndPath("block_factorys_bosses", "ice_gauntlet"),
            ResourceLocation.fromNamespaceAndPath("block_factorys_bosses", "sandworm_gauntlet"),
            ResourceLocation.fromNamespaceAndPath("legendary_monsters", "fiery_jaw"));

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ResourceLocation id : GLOVES) {
                Item glove = BuiltInRegistries.ITEM.get(id);
                if (glove != Items.AIR) CuriosRendererRegistry.register(glove, CopperGloveRenderer::new);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
            PoseStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buffer, int packedLight,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> model)) return;
        boolean left = slotContext.index() % 2 == 1;
        HumanoidArm arm = left ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        poseStack.pushPose();
        model.translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.translate((left ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        // the Copper Glove is small and sinks into the second skin layer: bigger, and a little further out
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(GLOVES.get(0))) {
            poseStack.translate(0.0f, 0.03f, 0.0f);
            poseStack.scale(1.05f, 1.05f, 1.05f);
        }
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(slotContext.entity(), stack,
                left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                left, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
