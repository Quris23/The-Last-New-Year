package dev.qur1s.spellclasses.mixin;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import dev.qur1s.spellclasses.client.QuestReveal;
import dev.qur1s.spellclasses.data.ClassAttachments;
import dev.qur1s.spellclasses.data.PersonalQuestProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared "История" chapter, client side. Until a quest is completed it shows no text (the description index that drives the
 * quest window is emptied); once completed, the subtitle under the title reads "Выполнил: Ник". The icon and title of a quest
 * are cached by FTB from its task, so while they are being computed the tasks must not be hidden (see {@link QuestReveal}).
 */
@Mixin(value = Quest.class, remap = false)
public abstract class QuestCompleterDescriptionMixin {
    private boolean spellclasses$shared() {
        return FtbQuestsClassGate.isSharedChapter(((Quest) (Object) this).getChapter().getId());
    }

    private boolean spellclasses$completed() {
        var player = Minecraft.getInstance().player;
        return player != null && PersonalQuestProgress.isCompleted(player, ((Quest) (Object) this).id);
    }

    /**
     * The quest is not on the page at all until it is completed (editors in edit mode still see everything). FTB draws a quest
     * only if this says it is visible, so it also drops its tile, its connections and its tooltip.
     */
    @Inject(method = "isVisible", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$hideUntilCompleted(dev.ftb.mods.ftbquests.quest.TeamData data, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !spellclasses$shared()) return;
        var file = dev.ftb.mods.ftbquests.client.ClientQuestFile.INSTANCE;
        if (file != null && file.canEdit()) return;
        if (!spellclasses$completed()) cir.setReturnValue(false);
    }

    /** The quest window lays its description out from this index: an empty one means no text at all. */
    @Inject(method = "buildDescriptionIndex", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$hideDescription(CallbackInfoReturnable<List<Pair<Integer, Integer>>> cir) {
        if (spellclasses$shared() && !spellclasses$completed()) cir.setReturnValue(new ArrayList<>());
    }

    @Inject(method = "getSubtitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$subtitle(CallbackInfoReturnable<Component> cir) {
        if (!spellclasses$shared()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        Quest quest = (Quest) (Object) this;
        if (!PersonalQuestProgress.isCompleted(player, quest.id)) {
            cir.setReturnValue(Component.empty());
            return;
        }
        String name = player.getData(ClassAttachments.QUEST_COMPLETERS).get(Long.toString(quest.id));
        if (name != null) {
            cir.setReturnValue(Component.literal("Выполнил: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(name).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)));
        }
    }
}
