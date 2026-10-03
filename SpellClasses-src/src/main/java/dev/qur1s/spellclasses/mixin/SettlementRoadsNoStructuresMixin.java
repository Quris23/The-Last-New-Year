package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.SettlementRoadsGuard;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Do not let Settlement Roads hunt for villages in a world that cannot have any (see {@link SettlementRoadsGuard}). */
@Mixin(targets = "net.countered.settlementroads.helpers.StructureConnector", remap = false)
public abstract class SettlementRoadsNoStructuresMixin {
    @Inject(method = "cacheNewConnection", at = @At("HEAD"), cancellable = true, require = 0)
    private static void spellclasses$skipPointlessSearch(ServerLevel level, boolean locateAtPlayer, CallbackInfo ci) {
        if (!SettlementRoadsGuard.targetCanExist(level)) {
            ci.cancel();
        }
    }
}
