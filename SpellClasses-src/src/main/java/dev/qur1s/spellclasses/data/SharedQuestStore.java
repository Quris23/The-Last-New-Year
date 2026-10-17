package dev.qur1s.spellclasses.data;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server-wide progress of the shared "История" chapter: whoever does a task, it counts for every player. Each player still
 * reads it from his own {@link PersonalQuestData} attachment (so the book and the client sync work exactly as for the class
 * chapters); this store is the source of truth for players who are offline, and is copied into the attachment on login.
 */
public class SharedQuestStore extends SavedData {
    private static final String KEY = "spellclasses_shared_quests";

    private final Map<Long, Long> progress = new HashMap<>();
    private final Set<Long> started = new HashSet<>();
    private final Set<Long> completed = new HashSet<>();
    private final Map<String, String> completers = new HashMap<>();

    public static SharedQuestStore get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(new Factory<>(SharedQuestStore::new,
                (tag, provider) -> load(tag), null), KEY);
    }

    /** A task's progress changed: remember it and push it to everybody online. */
    public static void shareProgress(MinecraftServer server, long id, long value) {
        SharedQuestStore store = get(server);
        if (value <= 0L) store.progress.remove(id);
        else store.progress.put(id, value);
        store.setDirty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (PersonalQuestProgress.getProgress(player, id) != value) PersonalQuestProgress.setProgress(player, id, value);
        }
    }

    public static void shareStarted(MinecraftServer server, long id, boolean value) {
        SharedQuestStore store = get(server);
        if (value ? store.started.add(id) : store.started.remove(id)) store.setDirty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (PersonalQuestProgress.isStarted(player, id) != value) PersonalQuestProgress.setStarted(player, id, value);
        }
    }

    public static void shareCompleted(MinecraftServer server, long id, boolean value) {
        SharedQuestStore store = get(server);
        if (value ? store.completed.add(id) : store.completed.remove(id)) store.setDirty();
        if (!value && store.completers.remove(Long.toString(id)) != null) {
            store.setDirty();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) copyCompleters(store, player);
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (PersonalQuestProgress.isCompleted(player, id) != value) PersonalQuestProgress.setCompleted(player, id, value);
        }
    }

    /** Remembers who completed a quest (only the first one counts) and tells everybody online. */
    public static void shareCompleter(MinecraftServer server, long id, String name) {
        SharedQuestStore store = get(server);
        if (store.completers.putIfAbsent(Long.toString(id), name) != null) return;
        store.setDirty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            copyCompleters(store, player);
        }
    }

    private static void copyCompleters(SharedQuestStore store, ServerPlayer player) {
        var current = player.getData(ClassAttachments.QUEST_COMPLETERS);
        if (!current.equals(store.completers)) player.setData(ClassAttachments.QUEST_COMPLETERS, new HashMap<>(store.completers));
    }

    /**
     * Brings the stored state in line: a task is only completed/started while its quest is completed (a reset removes the quest
     * but used to leave the task flags behind, so the quest could not be earned again), and a nickname only stays for a
     * completed quest.
     */
    public void sanitize() {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) return;
        Chapter chapter = file.getChapter(FtbQuestsClassGate.HISTORY_CHAPTER);
        if (chapter == null) return;
        boolean changed = false;
        for (Quest quest : chapter.getQuests()) {
            boolean questDone = completed.contains(quest.id);
            for (Task task : quest.getTasksAsList()) {
                if (!questDone) {
                    changed |= completed.remove(task.id);
                    changed |= started.remove(task.id);
                    changed |= progress.remove(task.id) != null;
                }
            }
            if (!questDone) {
                changed |= started.remove(quest.id);
                changed |= completers.remove(Long.toString(quest.id)) != null;
            }
        }
        if (changed) setDirty();
    }

    /** Sets one player's data for the whole shared chapter exactly as stored (also clears flags a reset left behind). */
    public void reconcile(ServerPlayer player) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) return;
        Chapter chapter = file.getChapter(FtbQuestsClassGate.HISTORY_CHAPTER);
        if (chapter == null) return;
        for (Quest quest : chapter.getQuests()) {
            sync(player, quest.id, 0L, started.contains(quest.id), completed.contains(quest.id));
            for (Task task : quest.getTasksAsList()) {
                sync(player, task.id, progress.getOrDefault(task.id, 0L), started.contains(task.id), completed.contains(task.id));
            }
        }
        copyCompleters(this, player);
    }

    private static void sync(ServerPlayer player, long id, long value, boolean isStarted, boolean isCompleted) {
        if (value > 0L && PersonalQuestProgress.getProgress(player, id) != value) PersonalQuestProgress.setProgress(player, id, value);
        if (value <= 0L && PersonalQuestProgress.getProgress(player, id) != 0L) PersonalQuestProgress.setProgress(player, id, 0L);
        if (PersonalQuestProgress.isStarted(player, id) != isStarted) PersonalQuestProgress.setStarted(player, id, isStarted);
        if (PersonalQuestProgress.isCompleted(player, id) != isCompleted) PersonalQuestProgress.setCompleted(player, id, isCompleted);
    }

    /** Copies everything recorded so far into one player's own data (login). */
    public void applyTo(ServerPlayer player) {
        sanitize();
        reconcile(player);
        progress.forEach((id, value) -> {
            if (PersonalQuestProgress.getProgress(player, id) != value) PersonalQuestProgress.setProgress(player, id, value);
        });
        for (long id : started) {
            if (!PersonalQuestProgress.isStarted(player, id)) PersonalQuestProgress.setStarted(player, id, true);
        }
        for (long id : completed) {
            if (!PersonalQuestProgress.isCompleted(player, id)) PersonalQuestProgress.setCompleted(player, id, true);
        }
        copyCompleters(this, player);
    }

    private static SharedQuestStore load(CompoundTag tag) {
        SharedQuestStore store = new SharedQuestStore();
        ListTag list = tag.getList("progress", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            store.progress.put(entry.getLong("id"), entry.getLong("value"));
        }
        for (long id : tag.getLongArray("started")) store.started.add(id);
        for (long id : tag.getLongArray("completed")) store.completed.add(id);
        CompoundTag names = tag.getCompound("completers");
        for (String key : names.getAllKeys()) store.completers.put(key, names.getString(key));
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        progress.forEach((id, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("id", id);
            entry.putLong("value", value);
            list.add(entry);
        });
        tag.put("progress", list);
        tag.putLongArray("started", started.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("completed", completed.stream().mapToLong(Long::longValue).toArray());
        CompoundTag names = new CompoundTag();
        completers.forEach(names::putString);
        tag.put("completers", names);
        return tag;
    }
}
