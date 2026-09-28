package dev.qur1s.spellclasses.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Attached to every player: their own private FTB Quests progress for the class-gated ("Магия")
 * chapters, kept out of the shared party {@code TeamData} so one player finishing a class quest
 * doesn't finish it for teammates. Mirrors the three pieces of state FTB Quests itself tracks per
 * task/quest id (progress, started, completed) - see
 * {@link dev.qur1s.spellclasses.mixin.TeamDataPersonalProgressMixin}.
 */
public record PersonalQuestData(Map<Long, Long> taskProgress, Set<Long> started, Set<Long> completed) {
    public static final PersonalQuestData DEFAULT = new PersonalQuestData(Map.of(), Set.of(), Set.of());

    public static final Codec<PersonalQuestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(Long::parseLong, String::valueOf), Codec.LONG)
                    .optionalFieldOf("task_progress", Map.of()).forGetter(PersonalQuestData::taskProgress),
            Codec.LONG.listOf().xmap(list -> (Set<Long>) new HashSet<>(list), set -> new java.util.ArrayList<>(set))
                    .optionalFieldOf("started", new HashSet<>()).forGetter(PersonalQuestData::started),
            Codec.LONG.listOf().xmap(list -> (Set<Long>) new HashSet<>(list), set -> new java.util.ArrayList<>(set))
                    .optionalFieldOf("completed", new HashSet<>()).forGetter(PersonalQuestData::completed)
    ).apply(instance, PersonalQuestData::new));

    /** Synced to the owning client so its own book screen renders personal progress correctly. */
    public static final StreamCodec<RegistryFriendlyByteBuf, PersonalQuestData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_LONG, ByteBufCodecs.VAR_LONG), PersonalQuestData::taskProgress,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.VAR_LONG), PersonalQuestData::started,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.VAR_LONG), PersonalQuestData::completed,
            PersonalQuestData::new
    );

    public long getProgress(long id) {
        return taskProgress.getOrDefault(id, 0L);
    }

    public boolean isStarted(long id) {
        return started.contains(id);
    }

    public boolean isCompleted(long id) {
        return completed.contains(id);
    }

    public PersonalQuestData withProgress(long id, long progress) {
        Map<Long, Long> copy = new HashMap<>(taskProgress);
        if (progress <= 0L) {
            copy.remove(id);
        } else {
            copy.put(id, progress);
        }
        return new PersonalQuestData(copy, started, completed);
    }

    public PersonalQuestData withStarted(long id, boolean value) {
        Set<Long> copy = new HashSet<>(started);
        if (value) copy.add(id); else copy.remove(id);
        return new PersonalQuestData(taskProgress, copy, completed);
    }

    public PersonalQuestData withCompleted(long id, boolean value) {
        Set<Long> copy = new HashSet<>(completed);
        if (value) copy.add(id); else copy.remove(id);
        return new PersonalQuestData(taskProgress, started, copy);
    }
}
