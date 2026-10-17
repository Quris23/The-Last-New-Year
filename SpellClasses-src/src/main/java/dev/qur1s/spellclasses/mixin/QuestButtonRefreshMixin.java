package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A quest button reads its icon and title once, when the screen is built. For the shared "История" chapter they are re-read on
 * every frame, so a "?" turns into the real eye the moment the quest is completed, without reopening the book.
 */
@Mixin(value = QuestButton.class, remap = false)
public abstract class QuestButtonRefreshMixin {
    @Shadow
    @Final
    Quest quest;

    @Inject(method = "draw", at = @At("HEAD"), require = 0)
    private void spellclasses$refreshMystery(CallbackInfo ci) {
        if (quest == null || !FtbQuestsClassGate.isSharedChapter(quest.getChapter().getId())) return;
        Button button = (Button) (Object) this;
        button.setIcon(quest.getIcon());
        button.setTitle(quest.getTitle());
    }
}
