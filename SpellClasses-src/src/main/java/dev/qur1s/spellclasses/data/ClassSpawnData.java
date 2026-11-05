package dev.qur1s.spellclasses.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Where each class's shared spawn point is (an overworld position), found once and then remembered. */
public class ClassSpawnData extends SavedData {
    private static final String KEY = "spellclasses_spawns";

    private final Map<String, Long> spawns = new HashMap<>();

    public static ClassSpawnData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(new Factory<>(ClassSpawnData::new,
                (tag, provider) -> load(tag), null), KEY);
    }

    public Optional<BlockPos> get(String schoolId) {
        Long packed = spawns.get(schoolId);
        return packed == null ? Optional.empty() : Optional.of(BlockPos.of(packed));
    }

    public void set(String schoolId, BlockPos pos) {
        spawns.put(schoolId, pos.asLong());
        setDirty();
    }

    public boolean clear(String schoolId) {
        boolean removed = spawns.remove(schoolId) != null;
        if (removed) setDirty();
        return removed;
    }

    private static ClassSpawnData load(CompoundTag tag) {
        ClassSpawnData data = new ClassSpawnData();
        CompoundTag all = tag.getCompound("spawns");
        for (String key : all.getAllKeys()) data.spawns.put(key, all.getLong(key));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag all = new CompoundTag();
        spawns.forEach(all::putLong);
        tag.put("spawns", all);
        return tag;
    }
}
