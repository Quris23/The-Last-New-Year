package dev.qur1s.sphereshields;

/**
 * Implemented by {@code ShieldGeneratorBlockEntity} via
 * {@link dev.qur1s.sphereshields.mixin.AccessControlDataMixin} - mirrors {@link AllowedPlayersHolder}'s
 * pattern but for a single flat toggle instead of a per-player set.
 * <p>
 * By user design: true lets any mob pass through the dome freely, regardless of the player whitelist -
 * {@link HostileMobBarrier} skips ejecting hostile mobs entirely when this is set, the same way it
 * already skips whitelisted players.
 */
public interface MobAccessHolder {
    boolean sphereshields$isAllowMobs();

    void sphereshields$setAllowMobs(boolean allow);
}
