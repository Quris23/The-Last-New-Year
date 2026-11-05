package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Cinematic Interactions draws items lying on a table at a fixed size per item, for a flat 16 px item. The Atlas is a
 * 3D book with a thick model and its own "fixed" display transform, so it came out far too large on the cartography table.
 */
@Mixin(targets = "dev.dyslexic.cinematic.client.ItemPlacer", remap = false)
public abstract class CinematicAtlasScaleMixin {
    private static final float ATLAS_SCALE = 0.55f;

    @WrapOperation(method = {"render", "drawOriented"}, remap = false, require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private static void spellclasses$smallerAtlas(ItemRenderer renderer, ItemStack stack, ItemDisplayContext context, boolean left,
                                                  PoseStack pose, MultiBufferSource buffers, int light, int overlay, BakedModel model,
                                                  Operation<Void> original) {
        boolean atlas = !stack.isEmpty() && "atlas".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
        if (!atlas) {
            original.call(renderer, stack, context, left, pose, buffers, light, overlay, model);
            return;
        }
        pose.pushPose();
        pose.scale(ATLAS_SCALE, ATLAS_SCALE, ATLAS_SCALE);
        original.call(renderer, stack, context, left, pose, buffers, light, overlay, model);
        pose.popPose();
    }
}
