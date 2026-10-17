package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasLiveEntry;
import net.minecraft.server.level.ServerPlayer;
import uk.co.iceconchy.aerowarptics.gate.RiftGate;
import uk.co.iceconchy.aerowarptics.gate.RiftGateRegistry;

import java.util.List;

/** AeroWarptics' Rift Gates the player may see, in one dimension. Only touched when AeroWarptics is loaded. */
public final class AtlasGates {
    private AtlasGates() {
    }

    public static void collect(ServerPlayer player, String dimension, List<AtlasLiveEntry> out) {
        for (RiftGate gate : RiftGateRegistry.get(player.getServer()).all()) {
            if (!gate.enabled() || !gate.isVisibleTo(player)) continue;
            if (!gate.dimension().location().toString().equals(dimension)) continue;
            var pos = gate.controller();
            out.add(new AtlasLiveEntry(AtlasLiveEntry.GATE, gate.id(), pos.getX(), pos.getY(), pos.getZ(), gate.displayName()));
        }
    }
}
