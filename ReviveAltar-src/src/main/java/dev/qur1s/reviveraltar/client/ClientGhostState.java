package dev.qur1s.reviveraltar.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Client-side cache of which players are currently ghosts, kept up to date via GhostSyncPayload. */
public final class ClientGhostState {
    private static final Set<UUID> GHOSTS = Collections.synchronizedSet(new HashSet<>());

    private ClientGhostState() {}

    public static void set(UUID player, boolean ghost) {
        if (ghost) {
            GHOSTS.add(player);
        } else {
            GHOSTS.remove(player);
        }
    }

    public static boolean isGhost(UUID player) {
        return GHOSTS.contains(player);
    }

    public static void clear() {
        GHOSTS.clear();
    }
}
