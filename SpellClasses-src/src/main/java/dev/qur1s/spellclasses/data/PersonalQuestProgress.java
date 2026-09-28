package dev.qur1s.spellclasses.data;

import net.minecraft.world.entity.player.Player;

/** Read/write helpers for {@link ClassAttachments#PERSONAL_QUEST_PROGRESS}. */
public final class PersonalQuestProgress {
    private PersonalQuestProgress() {
    }

    public static long getProgress(Player player, long id) {
        return player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).getProgress(id);
    }

    public static boolean isStarted(Player player, long id) {
        return player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).isStarted(id);
    }

    public static boolean isCompleted(Player player, long id) {
        return player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).isCompleted(id);
    }

    public static void setProgress(Player player, long id, long progress) {
        player.setData(ClassAttachments.PERSONAL_QUEST_PROGRESS,
                player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).withProgress(id, progress));
    }

    public static void setStarted(Player player, long id, boolean value) {
        player.setData(ClassAttachments.PERSONAL_QUEST_PROGRESS,
                player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).withStarted(id, value));
    }

    public static void setCompleted(Player player, long id, boolean value) {
        player.setData(ClassAttachments.PERSONAL_QUEST_PROGRESS,
                player.getData(ClassAttachments.PERSONAL_QUEST_PROGRESS).withCompleted(id, value));
    }
}
