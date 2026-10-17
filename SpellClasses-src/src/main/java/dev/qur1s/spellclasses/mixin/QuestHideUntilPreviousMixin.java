package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * "Акт 1": a quest with dependencies is on the page only once it has been reached: it is itself completed, or one of the
 * quests it depends on is completed (it is next in line), or one of the quests that depend on it is completed (it was
 * skipped over). Quests without dependencies are always shown. FTB's own visibility cannot do this: "hide until
 * dependencies complete" is defeated by {@link QuestOptionalDependenciesMixin}, and its "dependencies must be visible"
 * rule would hide the neighbours of an out-of-order completed quest, so for this chapter the rule below replaces it
 * (editors in edit mode still see everything).
 */
@Mixin(value = Quest.class, remap = false)
public abstract class QuestHideUntilPreviousMixin {
    /** The quest's own "Hide Quest Until Dependencies Complete" setting. */
    @Shadow
    private Tristate hideUntilDepsComplete;

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$visibleOnceReached(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        Quest self = (Quest) (Object) this;
        if (!FtbQuestsClassGate.isFreeFlowChapter(self.getChapter().getId()) || !self.hasDependencies()) return;
        var file = dev.ftb.mods.ftbquests.client.ClientQuestFile.INSTANCE;
        if (file != null && file.canEdit()) return;
        // Act 2 sets the flag on every quest (so it shows in the editor); there only the final Eye needs ALL dependencies
        boolean strict = hideUntilDepsComplete == Tristate.TRUE
                && (self.getChapter().getId() != FtbQuestsClassGate.ACT_TWO_CHAPTER || self.getId() == 0x62E099C44C866A43L);
        if (strict) {
            // set on the quest itself: shown only once it is done or EVERY quest it depends on is really completed
            // (optional dependencies count here too, unlike for unlocking)
            cir.setReturnValue(data.isCompleted(self) || self.streamDependencies().allMatch(data::isCompleted));
            return;
        }
        boolean reached = data.isCompleted(self)
                || self.streamDependencies().anyMatch(data::isCompleted);
        if (!reached) {
            for (QuestObject dependant : self.getDependants()) {
                if (data.isCompleted(dependant)) {
                    reached = true;
                    break;
                }
            }
        }
        cir.setReturnValue(reached);
    }
}
