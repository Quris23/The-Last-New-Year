package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import uk.co.iceconchy.aerowarptics.client.fx.RiftEffectManager;

/**
 * {@code RiftEffectManager.drawFire()} renders a separate "ember/fire" overlay pass on top of the
 * already-patched green throat/face geometry ({@code drawThroat}/{@code drawFace}, which correctly
 * use {@code ActiveRift.colour}/{@code accentColour} - already green via
 * {@link RiftModulatorDefaultColourMixin}/{@link RiftDriveTierColoursMixin}). Unlike those, drawFire's
 * red channel is hardcoded rather than derived from the rift's own colour at two points, by the mod's
 * own design (it only ever extracts the accent colour's green/blue channels, never red, and always
 * maxes red to 1.0f) - producing an orange/fire look regardless of the configured colour theme:
 * <ul>
 *   <li>The first of three {@code Mth.lerp(edge, red*0.15f, UPPER)} calls that blend the fire "band"
 *       colour per-ring uses a hardcoded {@code 1.0f} upper bound for red (the other two correctly use
 *       the accent colour's green/blue channels for their own upper bounds).</li>
 *   <li>The four corner calls to the private {@code vertex(...)} helper for the fire "lick" tip quads
 *       pass a hardcoded {@code 1.0f} red argument directly, alongside the (already green) accent
 *       green/blue channels.</li>
 * </ul>
 * By user design, both pulled down to a low value instead, so the fire overlay reads as light green
 * (dominated by the green/dark-green accent channels already in place) rather than orange.
 */
@Mixin(value = RiftEffectManager.class, remap = false)
public abstract class RiftEffectManagerFireColourMixin {
    private static final float LOW_RED = 0.2f;

    @ModifyArg(method = "drawFire", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 0), index = 2)
    private static float spellclasses$fireBandRedUpper(float upper) {
        return LOW_RED;
    }

    @ModifyArg(method = "drawFire", at = @At(value = "INVOKE",
            target = "Luk/co/iceconchy/aerowarptics/client/fx/RiftEffectManager;vertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Luk/co/iceconchy/aerowarptics/client/fx/RiftEffectManager$ActiveRift;FDDFFFF)V"),
            index = 6)
    private static float spellclasses$fireLickTipRed(float red) {
        return LOW_RED;
    }
}
