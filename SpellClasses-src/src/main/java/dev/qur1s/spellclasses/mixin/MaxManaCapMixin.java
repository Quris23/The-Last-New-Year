package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * 999 is a hard server-wide cap on max mana, regardless of how many sources (gear, the Laplace
 * Ring's +2000, ...) add to it — the attribute's own range clamps every read, so nothing needs to
 * special-case this elsewhere.
 */
@Mixin(value = MagicRangedAttribute.class, remap = false)
public abstract class MaxManaCapMixin {
    private static final double MAX_MANA_CAP = 2000.0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spellclasses$capMaxMana(String descriptionId, double defaultValue, double min, double max, CallbackInfo ci) {
        if (!"attribute.irons_spellbooks.max_mana".equals(descriptionId)) return;
        try {
            Field maxValueField = RangedAttribute.class.getDeclaredField("maxValue");
            maxValueField.setAccessible(true);
            double current = maxValueField.getDouble(this);
            if (current > MAX_MANA_CAP) {
                maxValueField.setDouble(this, MAX_MANA_CAP);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
