package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uk.co.iceconchy.aerowarptics.client.fx.WarpEffects;

/**
 * {@code WarpEffects.tearFire()} - run every client tick while a ship is actually inside the rift
 * corridor ({@code RIFT_TRANSIT} stage, i.e. the "flying through the wormhole" visual) - spawns
 * vanilla {@code ParticleTypes.ELECTRIC_SPARK} (the lightning-rod spark, yellow) alongside the mod's
 * own colour-tinted {@code RIFT_SPARK} particle. {@code aperture()} (gate opening) and {@code onStage()}
 * (failed-warp scatter) do the same. Unlike every other visual in this effects pipeline, that vanilla
 * particle is never tinted by the rift's own colour (own hardcoded texture, own renderer) - so
 * recoloring the rift green/dark green (see {@link RiftModulatorDefaultColourMixin},
 * {@link RiftDriveTierColoursMixin}, and the portal/fluid texture recolor in kubejs/assets) still left
 * a yellow flicker in the wormhole itself. By user design, dropped entirely rather than reimplemented
 * with a tinted replacement.
 */
@Mixin(value = WarpEffects.class, remap = false)
public abstract class WarpEffectsElectricSparkMixin {

    @Redirect(method = {"tearFire", "aperture", "onStage"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private static void spellclasses$dropElectricSpark(ClientLevel level, ParticleOptions options,
            double x, double y, double z, double dx, double dy, double dz) {
        if (options == ParticleTypes.ELECTRIC_SPARK) return;
        level.addParticle(options, x, y, z, dx, dy, dz);
    }
}
