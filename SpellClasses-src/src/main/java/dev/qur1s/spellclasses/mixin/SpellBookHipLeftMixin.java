package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * The worn spell book is drawn on the wearer's right hip (x = -5.5/16, or -4.5/16 without a chestplate).
 * Mirroring the offset and the book's tilt moves it to the left hip.
 */
@Mixin(value = SpellBookCurioRenderer.class, remap = false)
public abstract class SpellBookHipLeftMixin {
    @ModifyConstant(method = "render", constant = @Constant(doubleValue = -5.5), require = 0)
    private double spellclasses$leftWithChestplate(double x) {
        return 5.5;
    }

    @ModifyConstant(method = "render", constant = @Constant(doubleValue = -4.5), require = 0)
    private double spellclasses$leftWithoutChestplate(double x) {
        return 4.5;
    }

    @ModifyConstant(method = "render", constant = @Constant(floatValue = 3.0543263f), require = 0)
    private float spellclasses$mirrorTilt(float angle) {
        return -angle;
    }
}
