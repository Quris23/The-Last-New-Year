package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Imbuing at the Arcane Anvil is allowed for exactly one item: Iron's Spellbooks' Ring of
 * Affinity (irons_spellbooks:affinity_ring). Nothing else - not swords, not any other
 * non-armor item, not armor - regardless of the mod's own built-in spell-container/tag rules.
 */
@Mixin(targets = "io.redspace.ironsspellbooks.api.util.Utils", remap = false)
public class ImbueRestrictionMixin {
    private static final ResourceLocation AFFINITY_RING = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "affinity_ring");

    @Inject(method = "canImbue", at = @At("HEAD"), cancellable = true)
    private static void spellclasses$restrictImbue(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (!itemStack.is(BuiltInRegistries.ITEM.get(AFFINITY_RING))) {
            cir.setReturnValue(false);
        }
    }
}
