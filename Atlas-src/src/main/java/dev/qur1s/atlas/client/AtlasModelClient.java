package dev.qur1s.atlas.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/** The atlas is a 3D book in the hand, on the ground and in frames, but a flat 2D icon in inventory slots and the hotbar. */
@EventBusSubscriber(modid = "atlas", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AtlasModelClient {
    private static final ModelResourceLocation ICON = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath("atlas", "item/atlas_gui"));
    private static final ModelResourceLocation ITEM = ModelResourceLocation.inventory(
            ResourceLocation.fromNamespaceAndPath("atlas", "atlas"));

    private AtlasModelClient() {
    }

    @SubscribeEvent
    static void register(ModelEvent.RegisterAdditional event) {
        event.register(ICON);
    }

    @SubscribeEvent
    static void wrap(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        BakedModel book = models.get(ITEM);
        BakedModel icon = models.get(ICON);
        if (book != null && icon != null) models.put(ITEM, new Wrapper(book, icon));
    }

    private static final class Wrapper extends BakedModelWrapper<BakedModel> {
        private final BakedModel icon;

        Wrapper(BakedModel book, BakedModel icon) {
            super(book);
            this.icon = icon;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            if (context == ItemDisplayContext.GUI) return icon.applyTransform(context, poseStack, leftHand);
            return super.applyTransform(context, poseStack, leftHand);
        }
    }
}
