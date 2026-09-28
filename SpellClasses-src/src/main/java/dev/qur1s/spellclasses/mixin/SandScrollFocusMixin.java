package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSSchoolRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cataclysm: Spellbooks' Sand school shares Nature's own SchoolType focus tag at the Java level
 * (ModTags.NATURE_FOCUS, i.e. Poisonous Potato) - the same trick it uses to reuse Nature's
 * spell-power attribute for its own damage scaling. Scroll Forge crafting for Sand-school spells
 * should use the Ancient Metal Ingot as its focus reagent instead, without touching the real
 * Nature school's own check (both schools' SchoolType instances are distinct singletons, so an
 * identity check against CSSchoolRegistry.SAND.get() safely isolates just the Sand case).
 */
@Mixin(value = SchoolType.class, remap = false)
public abstract class SandScrollFocusMixin {
    private static final ResourceLocation ANCIENT_METAL_INGOT = ResourceLocation.fromNamespaceAndPath("cataclysm", "ancient_metal_ingot");

    @Inject(method = "isFocus", at = @At("HEAD"), cancellable = true)
    private void spellclasses$sandUsesAncientMetal(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == CSSchoolRegistry.SAND.get()) {
            cir.setReturnValue(itemStack.is(BuiltInRegistries.ITEM.get(ANCIENT_METAL_INGOT)));
        }
    }
}
