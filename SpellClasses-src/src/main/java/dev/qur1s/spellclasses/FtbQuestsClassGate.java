package dev.qur1s.spellclasses;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each per-class FTB Quests chapter (under the "Магия" chapter group) to the spell school
 * it's gated to. Read by {@link dev.qur1s.spellclasses.mixin.FtbQuestsChapterVisibilityMixin}
 * (the chapter is shown once the player has the class) and by the personal-progress mixin. Teams are not used any more,
 * so there is no separate per-player submission gate.
 */
public final class FtbQuestsClassGate {
    private FtbQuestsClassGate() {
    }

    private static final Map<Long, ResourceLocation> CLASS_CHAPTERS = new HashMap<>();

    static {
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("52A3E9D65E86AFC4", 16), school("ice"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("6056220CF06A0917", 16), school("fire"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("373774E94F304EFC", 16), school("lightning"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("7A8AC9B64D5FD26A", 16), school("nature"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("79667D19722FA0FB", 16), school("ender"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("2F6921C81B308D38", 16), school("blood"));
        CLASS_CHAPTERS.put(Long.parseUnsignedLong("2FADCD87CDAFAD07", 16), school("holy"));
    }

    /** The shared "История" chapter: progress is the same for every player on the server. */
    public static final long ACT_ONE_CHAPTER = 0x3C257F92BA6FB832L;
    public static final long ACT_TWO_CHAPTER = 0x5C68A55E22912FB1L;
    /** The two "История" quests (Око Безграничного Знания, Око Потерянной Технологии) that must be done before Акт 2 opens. */
    public static final long ACT_THREE_CHAPTER = 0x021F60413AD648B0L;
    /** «Око Истинной Скверны» в «Пути на край света»: Акт 3 закрыт, пока оно не выполнено. */
    public static final long ACT_THREE_GATE_QUEST = 0x1157E00000000003L;
    public static final long[] ACT_TWO_GATE_QUESTS = {0x1157E00000000001L, 0x1157E00000000004L};

    /** Chapters where optional dependencies don't block and a quest shows up once it is reached (Акт 1, Акт 2). */
    public static boolean isFreeFlowChapter(long chapterId) {
        return chapterId == ACT_ONE_CHAPTER || chapterId == ACT_TWO_CHAPTER;
    }
    public static final long HISTORY_CHAPTER = Long.parseUnsignedLong("4B1570A10000C001", 16);

    public static boolean isSharedChapter(long chapterId) {
        return chapterId == HISTORY_CHAPTER;
    }

    /** True for a class chapter (progress kept per player) and for the shared chapter (kept per player but mirrored to all). */
    public static boolean isPersonalChapter(long chapterId) {
        return CLASS_CHAPTERS.containsKey(chapterId) || chapterId == HISTORY_CHAPTER;
    }

    public static boolean isPersonalObject(QuestObject object) {
        if (object instanceof Task task) return isPersonalChapter(task.getQuestChapter().getId());
        if (object instanceof Quest quest) return isPersonalChapter(quest.getChapter().getId());
        return false;
    }

    public static boolean isSharedObject(QuestObject object) {
        if (object instanceof Task task) return isSharedChapter(task.getQuestChapter().getId());
        if (object instanceof Quest quest) return isSharedChapter(quest.getChapter().getId());
        return false;
    }

    private static ResourceLocation school(String path) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", path);
    }

    /** Null if {@code chapterId} isn't one of the class-gated chapters. */
    public static ResourceLocation requiredSchoolFor(long chapterId) {
        return CLASS_CHAPTERS.get(chapterId);
    }

    /**
     * Null if {@code object} (a {@link Task} or {@link Quest} - the only two {@link QuestObject}
     * kinds that live inside a chapter) isn't part of one of the class-gated chapters.
     */
    public static ResourceLocation requiredSchoolForObject(QuestObject object) {
        if (object instanceof Task task) {
            return requiredSchoolFor(task.getQuestChapter().getId());
        }
        if (object instanceof Quest quest) {
            return requiredSchoolFor(quest.getChapter().getId());
        }
        return null;
    }
}
