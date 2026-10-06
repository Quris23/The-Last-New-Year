package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Raises the vanilla armor attribute's hard cap from 30 to 60 (3 full armor bars) — overloadedarmorbar only renders past 30, doesn't raise the cap itself. */
@Mixin(value = RangedAttribute.class, remap = false)
public abstract class ArmorCapMixin {
    private static final double NEW_ARMOR_CAP = 60.0;

    @Shadow
    @Mutable
    private double maxValue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spellclasses$raiseArmorCap(String descriptionId, double defaultValue, double min, double max, CallbackInfo ci) {
        if ("attribute.name.generic.armor".equals(descriptionId)) {
            this.maxValue = NEW_ARMOR_CAP;
        }
    }
}
