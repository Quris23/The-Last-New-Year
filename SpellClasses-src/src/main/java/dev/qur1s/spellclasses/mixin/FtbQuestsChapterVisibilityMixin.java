package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.qur1s.spellclasses.ClassManager;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import dev.qur1s.spellclasses.FtbQuestsUniversalGate;
import dev.qur1s.spellclasses.data.ClassAttachments;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Team-wide visibility: a class-gated chapter is shown if any online team member has the class;
 * a universal item-gated chapter (Abyssal, Sand) is shown if any online team member has ever
 * obtained the matching item (see {@link dev.qur1s.spellclasses.UniversalItemPickupEvents}).
 */
@Mixin(value = Chapter.class, remap = false)
public abstract class FtbQuestsChapterVisibilityMixin {
    @Inject(method = "isVisible", at = @At("RETURN"), cancellable = true)
    private void spellclasses$hideOtherClassChapters(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        long chapterId = ((Chapter) (Object) this).getId();

        ResourceLocation requiredSchool = FtbQuestsClassGate.requiredSchoolFor(chapterId);
        if (requiredSchool != null) {
            boolean anyMemberHasClass = data.getOnlineMembers().stream()
                    .anyMatch(member -> ClassManager.chosenSchool(member).map(requiredSchool::equals).orElse(false));
            if (!anyMemberHasClass) {
                cir.setReturnValue(false);
            }
            return;
        }

        if (chapterId == FtbQuestsClassGate.ACT_TWO_CHAPTER) {
            // Акт 2 stays closed until both gate quests in "История" are completed
            for (long questId : FtbQuestsClassGate.ACT_TWO_GATE_QUESTS) {
                var quest = data.getFile().getQuest(questId);
                if (quest == null || !data.isCompleted(quest)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
            return;
        }

        if (chapterId == FtbQuestsClassGate.ACT_THREE_CHAPTER) {
            // Акт 3 stays closed until the Eye of True Corruption quest in "История" is completed
            var quest = data.getFile().getQuest(FtbQuestsClassGate.ACT_THREE_GATE_QUEST);
            if (quest == null || !data.isCompleted(quest)) cir.setReturnValue(false);
            return;
        }

        if (FtbQuestsUniversalGate.isUniversalChapter(chapterId)) {
            // Abyssal, Sand and Eldritch: hidden until at least one of their quests has been completed (their first
            // quests complete by obtaining the item, so the chapter opens itself).
            Chapter chapter = (Chapter) (Object) this;
            boolean anyCompleted = chapter.getQuests().stream().anyMatch(data::isCompleted);
            if (!anyCompleted) {
                cir.setReturnValue(false);
            }
        }
    }
}
