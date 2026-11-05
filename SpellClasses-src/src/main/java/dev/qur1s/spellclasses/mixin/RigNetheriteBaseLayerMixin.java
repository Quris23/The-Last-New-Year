package dev.qur1s.spellclasses.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Netherite backtanks (Create's and Octo Arms' rig) are "layered" armor: the plain netherite chestplate texture plus a diving layer
 * ({@code create:netherite_diving_layer_*}) on top, so besides the tank you also see a chestplate. For the netherite ones both layers are
 * swapped for a fully transparent texture; the tank itself is a 3D model drawn by Create's backtank layer and stays.
 */
@Mixin(targets = {"dev.tentaworks.rig.TentacleRigItem$Layered", "com.simibubi.create.content.equipment.armor.BacktankItem$Layered"}, remap = false)
public abstract class RigNetheriteBaseLayerMixin {
    private static final ResourceLocation BLANK = ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/models/armor/blank_layer.png");

    private static boolean isNetherite(ItemStack stack) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("netherite");
    }

    /** overrides the default armor-texture lookup of the item */
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        ResourceLocation texture = layer.texture(innerModel);
        if (!isNetherite(stack)) return texture;
        if ("minecraft".equals(texture.getNamespace()) && texture.getPath().contains("/netherite_layer_")) return BLANK;
        return texture;
    }

    /** the second (diving) layer of the layered armor: the path of its texture */
    @Inject(method = "getArmorTextureLocation", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$blankDivingLayer(LivingEntity entity, EquipmentSlot slot, ItemStack stack, int layer, CallbackInfoReturnable<String> cir) {
        if (!isNetherite(stack)) return;
        cir.setReturnValue("spellclasses:textures/models/armor/blank_layer.png");
    }
}
