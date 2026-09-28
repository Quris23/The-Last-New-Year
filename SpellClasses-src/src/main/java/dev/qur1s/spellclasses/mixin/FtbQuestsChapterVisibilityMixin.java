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

        ResourceLocation requiredItem = FtbQuestsUniversalGate.requiredItemFor(chapterId);
        if (requiredItem != null) {
            String requiredItemId = requiredItem.toString();
            boolean anyMemberHasItem = data.getOnlineMembers().stream()
                    .anyMatch(member -> member.getData(ClassAttachments.OBTAINED_UNIVERSAL_ITEMS).contains(requiredItemId));
            if (!anyMemberHasItem) {
                cir.setReturnValue(false);
            }
        }
    }
}
