package dev.qur1s.spellclasses;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all currently-ticking Northstar Temperature Regulators, keyed by position and fed once
 * per regulator per game tick from {@link dev.qur1s.spellclasses.mixin.TemperatureRegulatorColdSweatSyncMixin}.
 * A single central player-tick hook ({@link NorthstarRegulatorColdSweatSync}) reads this registry and
 * applies at most one Cold Sweat WORLD override per player.
 * <p>
 * This split exists because letting each regulator block entity write (and un-write) the override
 * independently caused a race: every regulator's tick blanket-cleared ALL players' WORLD modifiers
 * before re-adding its own, so with multiple regulators in a level only whichever one happened to tick
 * last in that game tick actually stuck - reported as "only the most recently placed/re-placed
 * regulator works, older ones stop affecting players who stand in them."
 * <p>
 * Entries are pruned by staleness ({@code lastUpdateTick} too old, or the stored block entity reports
 * itself removed) rather than through an explicit removal hook, since a regulator's {@code tick()}
 * simply stops running the moment it's broken/unloaded.
 */
public final class RegulatorSyncTracker {
    private RegulatorSyncTracker() {
    }

    /** Max game ticks since a regulator's last registration before it's considered stale/gone. */
    public static final int STALE_AFTER_TICKS = 40;

    public record RegulatorInfo(TemperatureRegulatorBlockEntity regulator, boolean active, boolean powered,
                                 float temperature, long lastUpdateTick) {
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, RegulatorInfo>> REGULATORS = new ConcurrentHashMap<>();

    public static void update(ResourceKey<Level> dimension, BlockPos pos, RegulatorInfo info) {
        REGULATORS.computeIfAbsent(dimension, d -> new ConcurrentHashMap<>()).put(pos, info);
    }

    public static Collection<RegulatorInfo> regulatorsIn(ResourceKey<Level> dimension, long currentTick) {
        Map<BlockPos, RegulatorInfo> perDimension = REGULATORS.get(dimension);
        if (perDimension == null) return List.of();

        perDimension.values().removeIf(info -> info.regulator().isRemoved()
                || currentTick - info.lastUpdateTick() > STALE_AFTER_TICKS);
        return perDimension.values();
    }
}
