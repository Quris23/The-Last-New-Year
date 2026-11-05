package dev.qur1s.spellclasses.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An implant taken out of the body goes into the inventory: the client is sent the whole inventory right away, not only after a relog. */
@Pseudo
@Mixin(targets = "com.chromeware.cyberware.Installing", remap = false)
public abstract class InstallingResyncMixin {
    @Inject(method = "returnItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"), remap = false, require = 0)
    private static void spellclasses$resync(Player player, ItemStack item, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            sp.getInventory().setChanged();
            sp.inventoryMenu.broadcastFullState();
            if (sp.containerMenu != sp.inventoryMenu) sp.containerMenu.broadcastFullState();
        }
    }
}
