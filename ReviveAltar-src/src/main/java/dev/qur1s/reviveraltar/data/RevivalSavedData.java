package dev.qur1s.reviveraltar.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks which players are currently "dead and awaiting revival" (hardcore ghosts),
 * and any revivals that finished while the target player was offline.
 */
public class RevivalSavedData extends SavedData {
    private static final String ID = "reviveraltar_revivals";

    /** UUID -> last known player name, for players currently in ghost state. */
    private final Map<UUID, String> awaitingRevival = new HashMap<>();

    /** UUID -> dimension + block pos of the altar that completed the ritual, for offline players. */
    private final Map<UUID, PendingTeleport> pendingOffline = new HashMap<>();

    /** UUID -> the altar a (still living) player has pre-loaded with their own head + totem. */
    private final Map<UUID, PrimedAltar> primedAltars = new HashMap<>();

    public record PendingTeleport(ResourceKey<Level> dimension, double x, double y, double z) {}

    public record PrimedAltar(ResourceKey<Level> dimension, BlockPos pos) {}

    public static SavedData.Factory<RevivalSavedData> factory() {
        return new SavedData.Factory<>(RevivalSavedData::new, RevivalSavedData::load, null);
    }

    public static RevivalSavedData get(net.minecraft.server.level.ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(factory(), ID);
    }

    public boolean isAwaitingRevival(UUID uuid) {
        return awaitingRevival.containsKey(uuid);
    }

    public void markAwaitingRevival(UUID uuid, String name) {
        awaitingRevival.put(uuid, name);
        setDirty();
    }

    public void clearAwaitingRevival(UUID uuid) {
        awaitingRevival.remove(uuid);
        setDirty();
    }

    public String getName(UUID uuid) {
        return awaitingRevival.getOrDefault(uuid, "???");
    }

    public void queueOfflineRevival(UUID uuid, ResourceKey<Level> dimension, double x, double y, double z) {
        pendingOffline.put(uuid, new PendingTeleport(dimension, x, y, z));
        setDirty();
    }

    public PendingTeleport takePendingRevival(UUID uuid) {
        PendingTeleport t = pendingOffline.remove(uuid);
        if (t != null) {
            setDirty();
        }
        return t;
    }

    public void setPrimedAltar(UUID uuid, ResourceKey<Level> dimension, BlockPos pos) {
        primedAltars.put(uuid, new PrimedAltar(dimension, pos.immutable()));
        setDirty();
    }

    public PrimedAltar takePrimedAltar(UUID uuid) {
        PrimedAltar p = primedAltars.remove(uuid);
        if (p != null) {
            setDirty();
        }
        return p;
    }

    public void clearPrimedAltarAt(UUID uuid, BlockPos pos) {
        PrimedAltar p = primedAltars.get(uuid);
        if (p != null && p.pos().equals(pos)) {
            primedAltars.remove(uuid);
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag awaiting = new ListTag();
        for (Map.Entry<UUID, String> e : awaitingRevival.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.putString("name", e.getValue());
            awaiting.add(entry);
        }
        tag.put("awaiting", awaiting);

        ListTag pending = new ListTag();
        for (Map.Entry<UUID, PendingTeleport> e : pendingOffline.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.putString("dimension", e.getValue().dimension().location().toString());
            entry.putDouble("x", e.getValue().x());
            entry.putDouble("y", e.getValue().y());
            entry.putDouble("z", e.getValue().z());
            pending.add(entry);
        }
        tag.put("pending", pending);

        ListTag primed = new ListTag();
        for (Map.Entry<UUID, PrimedAltar> e : primedAltars.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.putString("dimension", e.getValue().dimension().location().toString());
            entry.putInt("x", e.getValue().pos().getX());
            entry.putInt("y", e.getValue().pos().getY());
            entry.putInt("z", e.getValue().pos().getZ());
            primed.add(entry);
        }
        tag.put("primed", primed);
        return tag;
    }

    private static RevivalSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        RevivalSavedData data = new RevivalSavedData();
        ListTag awaiting = tag.getList("awaiting", 10);
        for (int i = 0; i < awaiting.size(); i++) {
            CompoundTag entry = awaiting.getCompound(i);
            data.awaitingRevival.put(entry.getUUID("uuid"), entry.getString("name"));
        }
        ListTag pending = tag.getList("pending", 10);
        for (int i = 0; i < pending.size(); i++) {
            CompoundTag entry = pending.getCompound(i);
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(entry.getString("dimension")));
            data.pendingOffline.put(entry.getUUID("uuid"),
                    new PendingTeleport(dim, entry.getDouble("x"), entry.getDouble("y"), entry.getDouble("z")));
        }
        ListTag primed = tag.getList("primed", 10);
        for (int i = 0; i < primed.size(); i++) {
            CompoundTag entry = primed.getCompound(i);
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(entry.getString("dimension")));
            data.primedAltars.put(entry.getUUID("uuid"), new PrimedAltar(dim,
                    new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"))));
        }
        return data;
    }
}
