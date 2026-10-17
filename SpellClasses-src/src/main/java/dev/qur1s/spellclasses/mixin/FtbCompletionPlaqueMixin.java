package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.qur1s.spellclasses.client.FtbPlaque;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A completed FTB quest is shown as an Advancement Plaques plaque instead of FTB's own toast (when that mod is installed). */
@Mixin(value = FTBQuestsClient.class, remap = false)
public abstract class FtbCompletionPlaqueMixin {
    @Inject(method = "showCompletionToast", at = @At("HEAD"), cancellable = true, require = 0)
    private static void spellclasses$plaque(QuestObject object, CallbackInfo ci) {
        if (FtbPlaque.show(object)) ci.cancel();
    }
}
