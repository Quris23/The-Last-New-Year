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
 * (team-wide visibility: shown if any online member has the class) and
 * {@link dev.qur1s.spellclasses.mixin.FtbQuestsTaskGateMixin} (per-player: only completable by a
 * player who actually has that class themselves).
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
