package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.qur1s.spellclasses.ClassManager;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-player gate for a class-gated chapter's tasks: even though a teammate with a different
 * class can see the chapter (see {@link FtbQuestsChapterVisibilityMixin}), only a player who
 * actually has the matching class can submit its tasks.
 */
@Mixin(value = Task.class, remap = false)
public abstract class FtbQuestsTaskGateMixin {
    @Inject(method = "submitTask(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void spellclasses$blockWrongClassSubmit(TeamData teamData, ServerPlayer player, CallbackInfo ci) {
        ResourceLocation requiredSchool = FtbQuestsClassGate.requiredSchoolFor(((Task) (Object) this).getQuestChapter().getId());
        if (requiredSchool == null) return;
        boolean hasClass = ClassManager.chosenSchool(player).map(requiredSchool::equals).orElse(false);
        if (!hasClass) {
            ci.cancel();
        }
    }
}
