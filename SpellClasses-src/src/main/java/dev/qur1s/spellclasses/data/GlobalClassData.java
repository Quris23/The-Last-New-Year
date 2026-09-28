package dev.qur1s.spellclasses.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * World-wide record of which players have locked in which class, independent of who's currently
 * online. This is what a future per-class player cap (e.g. "only one Ender class") counts against:
 * see {@link dev.qur1s.spellclasses.ClassManager#canJoinSchool}.
 */
public class GlobalClassData extends SavedData {
    private static final String KEY = "spellclasses_data";

    private final Map<String, Set<UUID>> membersBySchool = new HashMap<>();

    public static GlobalClassData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(new Factory<>(GlobalClassData::new,
                (tag, provider) -> load(tag), null), KEY);
    }

    public int countInSchool(String schoolId) {
        return membersBySchool.getOrDefault(schoolId, Set.of()).size();
    }

    public boolean isMember(String schoolId, UUID player) {
        return membersBySchool.getOrDefault(schoolId, Set.of()).contains(player);
    }

    public void addMember(String schoolId, UUID player) {
        membersBySchool.computeIfAbsent(schoolId, s -> new HashSet<>()).add(player);
        setDirty();
    }

    public void removeMember(String schoolId, UUID player) {
        var members = membersBySchool.get(schoolId);
        if (members != null && members.remove(player)) setDirty();
    }

    private static GlobalClassData load(CompoundTag tag) {
        GlobalClassData data = new GlobalClassData();
        for (String schoolId : tag.getAllKeys()) {
            ListTag list = tag.getList(schoolId, 8); // 8 = string tag
            Set<UUID> members = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                members.add(UUID.fromString(list.getString(i)));
            }
            data.membersBySchool.put(schoolId, members);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (var entry : membersBySchool.entrySet()) {
            ListTag list = new ListTag();
            for (UUID id : entry.getValue()) list.add(StringTag.valueOf(id.toString()));
            tag.put(entry.getKey(), list);
        }
        return tag;
    }
}
