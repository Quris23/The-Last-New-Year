package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * {@code renderMetaballTraces} draws the thin scanline/outline segments along a sparse subset of
 * mesh triangle edges (every 7th/11th/17th triangle) as a "circuit board" flourish - unrelated to
 * our glyph overlay, but visually reads as stray diagonal lines cutting across (and between) the
 * glyphs. Silencing it entirely; the translucent surface fill (renderMetaballSurface) and our own
 * glyph pass are untouched.
 * <p>
 * The target method's first two parameters are {@code List<CachedTriangle>}/{@code List<ShieldImpact>}
 * - both package-private nested types this mixin's own package can't name. {@code @Coerce} lets the
 * handler declare them as raw {@code List} instead; we never touch the contents anyway.
 */
@Mixin(value = ShieldGeneratorRenderer.class, remap = false)
abstract class HideTriangleOutlinesMixin {
    @Inject(method = "renderMetaballTraces", at = @At("HEAD"), cancellable = true)
    private static void sphereshields$hideOutlines(
            @Coerce List triangles,
            @Coerce List impacts,
            float renderTick, float pulse, float impactEnergy,
            PoseStack poseStack, VertexConsumer lines,
            CallbackInfo ci) {
        ci.cancel();
    }
}
