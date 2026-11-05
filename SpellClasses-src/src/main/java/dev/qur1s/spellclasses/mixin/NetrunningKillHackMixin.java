package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.QuickhackKills;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Notes every landed killing quickhack (for the "kill with a quickhack" FTB quest). */
@Pseudo
@Mixin(targets = "com.chromeware.netrunning.Netrunning", remap = false)
public abstract class NetrunningKillHackMixin {
    @Inject(method = "apply(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lcom/chromeware/netrunning/Quickhack;)Z",
            at = @At("HEAD"), remap = false, require = 0)
    private static void spellclasses$noteHack(Player player, Entity target, com.chromeware.netrunning.Quickhack hack,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (QuickhackKills.killing(hack.name())) QuickhackKills.record(player, target);
    }
}
