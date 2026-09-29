package dev.qur1s.sphereshields;

import java.util.Set;
import java.util.UUID;

/**
 * Implemented by {@code ShieldGeneratorBlockEntity} via
 * {@link dev.qur1s.sphereshields.mixin.AccessControlDataMixin} (Mixin adds the {@code implements}
 * declared on a mixin class to the actual target class) - a plain interface like this one, defined
 * outside the mixin package, is what makes the cast usable from OTHER mixins/classes on the same
 * target, since a mixin class itself is never actually part of the runtime type hierarchy.
 * <p>
 * Whitelist semantics, by user design: a player NOT in this set gets pushed out of the dome exactly
 * like a hostile mob (see {@link HostileMobBarrier}) - an empty set means nobody is let in until the
 * owner explicitly adds players through the shield GUI's access button.
 */
public interface AllowedPlayersHolder {
    Set<UUID> sphereshields$getAllowedPlayers();

    void sphereshields$setAllowedPlayers(Set<UUID> allowed);
}
