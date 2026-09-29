package dev.qur1s.sphereshields;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight registry of currently-active ground (non-Sable) dome shields, keyed by generator
 * position. Fed by {@link dev.qur1s.sphereshields.mixin.GroundShieldTrackMixin} every tick and read
 * by {@link HostileMobBarrier} to push hostile mobs (and non-whitelisted players) back out of active
 * domes.
 * Kept entirely inside our own mod rather than touching shield_generators' own (private, unexposed)
 * ShieldRegistry, so this only ever needs to reason about the ground/hemisphere-turned-sphere case.
 */
public final class GroundShieldTracker {
    private GroundShieldTracker() {
    }

    public record Dome(BlockPos center, double radius, Set<UUID> allowedPlayers) {
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, Dome>> DOMES = new ConcurrentHashMap<>();

    public static void update(ResourceKey<Level> dimension, BlockPos generatorPos, double radius, Set<UUID> allowedPlayers) {
        DOMES.computeIfAbsent(dimension, d -> new ConcurrentHashMap<>())
                .put(generatorPos, new Dome(generatorPos, radius, allowedPlayers));
    }

    public static void remove(ResourceKey<Level> dimension, BlockPos generatorPos) {
        Map<BlockPos, Dome> perDimension = DOMES.get(dimension);
        if (perDimension != null) perDimension.remove(generatorPos);
    }

    public static java.util.Collection<Dome> domesIn(ResourceKey<Level> dimension) {
        Map<BlockPos, Dome> perDimension = DOMES.get(dimension);
        return perDimension == null ? java.util.List.of() : perDimension.values();
    }
}
