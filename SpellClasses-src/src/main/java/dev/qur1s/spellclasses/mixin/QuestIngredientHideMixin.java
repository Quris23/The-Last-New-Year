package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import dev.qur1s.spellclasses.data.PersonalQuestProgress;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * The item of a not yet completed quest in the shared "История" chapter cannot be looked up in JEI from the quest window
 * (hovering its task and pressing R/U) - otherwise the recipe would give away what the "?" is hiding.
 */
@Mixin(value = {Task.class, ItemTask.class}, remap = false)
public abstract class QuestIngredientHideMixin {
    @Inject(method = "getIngredient", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$hideIngredient(Widget widget, CallbackInfoReturnable<Optional<PositionedIngredient>> cir) {
        Task task = (Task) (Object) this;
        if (!FtbQuestsClassGate.isSharedChapter(task.getQuest().getChapter().getId())) return;
        var questFile = dev.ftb.mods.ftbquests.client.ClientQuestFile.INSTANCE;
        if (questFile != null && questFile.canEdit()) return;
        var player = Minecraft.getInstance().player;
        if (player != null && !PersonalQuestProgress.isCompleted(player, task.getQuest().id)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
