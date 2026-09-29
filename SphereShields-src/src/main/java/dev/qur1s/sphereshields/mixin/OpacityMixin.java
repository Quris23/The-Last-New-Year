package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * The dome surface is rendered almost fully transparent: a low base alpha (0.052f), capped at
 * 0.46f, then scaled again by 0.55f right before the vertex is emitted - roughly 0.1-0.2 effective
 * opacity in practice. Raises all three so the shield reads as a solid-looking surface instead of a
 * faint haze, while leaving the shimmer/flash animation terms (which add on top of the base) alone.
 */
@Mixin(value = ShieldGeneratorRenderer.class, remap = false)
abstract class OpacityMixin {
    @ModifyConstant(method = "renderMetaballSurface", constant = @Constant(floatValue = 0.052f))
    private static float sphereshields$higherBaseAlpha(float original) {
        return 0.12f;
    }

    @ModifyConstant(method = "renderMetaballSurface", constant = @Constant(floatValue = 0.46f))
    private static float sphereshields$higherAlphaCap(float original) {
        return 0.85f;
    }

    @ModifyConstant(method = "addSurfaceVertex", constant = @Constant(floatValue = 0.55f))
    private static float sphereshields$higherFinalScale(float original) {
        return 0.9f;
    }
}
