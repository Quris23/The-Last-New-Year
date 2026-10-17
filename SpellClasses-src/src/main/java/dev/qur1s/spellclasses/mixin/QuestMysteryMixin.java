package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import dev.qur1s.spellclasses.data.PersonalQuestProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quests of the shared "История" chapter stay a mystery until they are completed: a "?" icon and the title "???" instead of
 * the eye and its name (for the quest and for its task alike). {@link QuestCompleterDescriptionMixin} hides the texts.
 */
@Mixin(value = QuestObjectBase.class, remap = false)
public abstract class QuestMysteryMixin {
    private boolean spellclasses$hidden() {
        if (dev.qur1s.spellclasses.client.QuestReveal.active()) return false;
        var questFile = dev.ftb.mods.ftbquests.client.ClientQuestFile.INSTANCE;
        if (questFile != null && questFile.canEdit()) return false;
        Quest quest = null;
        if ((Object) this instanceof Quest q) quest = q;
        else if ((Object) this instanceof Task task) quest = task.getQuest();
        if (quest == null || !FtbQuestsClassGate.isSharedChapter(quest.getChapter().getId())) return false;
        var player = Minecraft.getInstance().player;
        boolean hidden = player != null && !PersonalQuestProgress.isCompleted(player, quest.id);
        if (dev.qur1s.spellclasses.client.QuestReveal.LOGGED.size() < 20 && dev.qur1s.spellclasses.client.QuestReveal.LOGGED.add(quest.id)) {
            org.apache.logging.log4j.LogManager.getLogger("SpellClasses/QuestMystery").info(
                    "quest {} hidden={} (player={}, completed={})", Long.toHexString(quest.id), hidden, player != null,
                    player != null && PersonalQuestProgress.isCompleted(player, quest.id));
        }
        return hidden;
    }

    @Inject(method = "getTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$hideTitle(CallbackInfoReturnable<Component> cir) {
        if (spellclasses$hidden()) cir.setReturnValue(Component.literal("???"));
    }

    @Inject(method = "getIcon", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$hideIcon(CallbackInfoReturnable<Icon> cir) {
        if (spellclasses$hidden()) cir.setReturnValue(dev.qur1s.spellclasses.client.QuestMysteryIcon.UNKNOWN);
    }
}
