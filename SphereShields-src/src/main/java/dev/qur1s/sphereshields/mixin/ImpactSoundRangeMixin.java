package dev.qur1s.sphereshields.mixin;

import dev.qur1s.sphereshields.GroundShieldTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The base mod's impact/tremor sounds ride vanilla {@code ServerLevel.playSound}'s volume-driven
 * broadcast radius (a fixed 16 blocks, or {@code 16 x volume} once volume exceeds 1.0 - there's no
 * separate distance parameter in the API). That's fine for the mod's own small hemisphere, but by
 * user design the impact sound should always be audible from anywhere inside our (much larger)
 * sphere: the worst case is standing on the exact opposite side of the dome from the impact point,
 * which is 2x the dome's radius away - so that's the target broadcast distance, achieved the same
 * way vanilla itself does it, by boosting the volume passed to playSound.
 * <p>
 * Only ground-mode domes get widened (matched by finding whichever tracked {@link GroundShieldTracker}
 * dome's surface the impact position lies closest to); Sable/Aeronautics shields are left untouched.
 */
@Mixin(targets = "com.anton.shieldgenerators.ShieldRegistry", remap = false)
abstract class ImpactSoundRangeMixin {
    @Redirect(
            method = {
                    "playImpactSound(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DDLnet/minecraft/util/RandomSource;)V",
                    "playTremorStutter(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;DDFDDILnet/minecraft/util/RandomSource;)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V")
    )
    private static void sphereshields$widenImpactSoundRange(ServerLevel level, Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        double radius = sphereshields$domeRadiusAt(level, pos);
        if (radius > 0.0) {
            float minVolumeForRadius = (float) ((radius * 2.0) / 16.0);
            if (minVolumeForRadius > volume) {
                volume = minVolumeForRadius;
            }
        }
        level.playSound(player, pos, sound, source, volume, pitch);
    }

    /** Finds the tracked dome whose surface the given position best matches (closest to its radius). */
    @Unique
    private static double sphereshields$domeRadiusAt(ServerLevel level, BlockPos pos) {
        double bestDelta = Double.MAX_VALUE;
        double bestRadius = -1.0;
        for (GroundShieldTracker.Dome dome : GroundShieldTracker.domesIn(level.dimension())) {
            double distance = Math.sqrt(dome.center().distSqr(pos));
            double delta = Math.abs(distance - dome.radius());
            if (delta < bestDelta) {
                bestDelta = delta;
                bestRadius = dome.radius();
            }
        }
        return bestRadius;
    }
}
