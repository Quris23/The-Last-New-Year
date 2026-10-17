package dev.qur1s.reviveraltar.mixin;

import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla's Player#interactOn only lets spectators open entities that implement MenuProvider,
 * silently ignoring everything else (e.g. a corpse mod's corpse entity). Ghosts should be able
 * to interact with the world like normal players (loot their own corpse, etc), so we make the
 * isSpectator() check inside interactOn report false for ghosts, without touching any of the
 * other spectator behavior (noclip flight, hidden HUD, no block interaction).
 */
@Mixin(Player.class)
public abstract class GhostInteractMixin {

    @Redirect(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
    private boolean reviveraltar$allowGhostEntityInteract(Player self) {
        if (RevivalManager.isGhost(self)) {
            return false;
        }
        return self.isSpectator();
    }
}
