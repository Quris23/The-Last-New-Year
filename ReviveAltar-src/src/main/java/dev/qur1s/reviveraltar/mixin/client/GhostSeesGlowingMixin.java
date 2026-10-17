package dev.qur1s.reviveraltar.mixin.client;

import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While the local player is a ghost, living players are rendered with the glowing
 * outline effect (same visual as the Spectral Arrow / Glowing status effect) so ghosts
 * can find survivors through walls.
 */
@Mixin(Entity.class)
public abstract class GhostSeesGlowingMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void reviveraltar$glowForGhosts(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player targetPlayer) || RevivalManager.isGhost(targetPlayer)) {
            return;
        }
        Player local = Minecraft.getInstance().player;
        if (local != null && RevivalManager.isGhost(local)) {
            cir.setReturnValue(true);
        }
    }
}
