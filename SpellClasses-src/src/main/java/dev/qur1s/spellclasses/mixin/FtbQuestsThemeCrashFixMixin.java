package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.quest.QuestShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * QuestShape.reload(list) crashes with NoSuchElementException (MAP.values().iterator().next() on
 * an empty map) if the theme resource stack came back empty on a given resource reload - happens
 * intermittently depending on mod load timing, before FTB Quests' own bundled theme resource is
 * registered. Skip the reload instead of crashing; a later reload (world join, F3+T, ...) with the
 * resources actually available will populate it correctly.
 */
@Mixin(value = QuestShape.class, remap = false)
public abstract class FtbQuestsThemeCrashFixMixin {
    @Inject(method = "reload", at = @At("HEAD"), cancellable = true)
    private static void spellclasses$skipEmptyThemeReload(List<String> list, CallbackInfo ci) {
        if (list.isEmpty()) {
            ci.cancel();
        }
    }
}
