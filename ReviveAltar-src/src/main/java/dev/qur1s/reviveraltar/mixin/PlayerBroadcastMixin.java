package dev.qur1s.reviveraltar.mixin;

import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ghost players are internally in Spectator mode, which vanilla only broadcasts to other
 * spectators. We want living players to still see (and hear about) the ghost, so force
 * visibility to everyone while the ghost flag is set.
 */
@Mixin(ServerPlayer.class)
public abstract class PlayerBroadcastMixin {

    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
    private void reviveraltar$forceGhostVisible(ServerPlayer viewer, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!viewer.isSpectator() && RevivalManager.isGhost(self)) {
            cir.setReturnValue(true);
        }
    }
}
