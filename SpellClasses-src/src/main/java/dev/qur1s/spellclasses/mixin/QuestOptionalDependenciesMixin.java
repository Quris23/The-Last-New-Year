package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.quest.DependencyRequirement;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In "Акт 1" a dependency marked optional keeps its line in the tree but no longer blocks the quest after it
 * (stock FTB Quests only uses "optional" to decide whether a chapter counts as finished).
 */
@Mixin(value = Quest.class, remap = false)
public abstract class QuestOptionalDependenciesMixin {
    @Shadow
    private DependencyRequirement dependencyRequirement;

    @Inject(method = "areDependenciesComplete(Ldev/ftb/mods/ftbquests/quest/TeamData;)Z", at = @At("HEAD"), cancellable = true)
    private void spellclasses$optionalDependenciesDontBlock(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        Quest self = (Quest) (Object) this;
        if (!FtbQuestsClassGate.isFreeFlowChapter(self.getChapter().getId())) return;
        DependencyRequirement requirement = dependencyRequirement;
        if (self.getMinRequiredDependencies() > 0 || requirement.needOnlyOne()) return;
        boolean needCompletion = requirement.needCompletion();
        cir.setReturnValue(self.streamDependencies().allMatch(dependency ->
                (dependency instanceof Quest quest && quest.isOptional())
                        || (needCompletion ? data.isCompleted(dependency) : data.isStarted(dependency))));
    }
}
