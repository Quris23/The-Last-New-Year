package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.ImplantBackpackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The upgrades of the hidden implant backpack are the implants themselves: nothing may be put into or taken out of its upgrade
 * slots in the Sophisticated screen (the implants are changed in the Chromeware menu).
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase$StorageUpgradeSlot", remap = false)
public abstract class ImplantBackpackUpgradeSlotMixin {
    @Shadow(remap = false)
    @Final
    StorageContainerMenuBase<?> this$0;

    private boolean spellclasses$implantBackpack() {
        return this.this$0.getStorageWrapper() instanceof IBackpackWrapper wrapper
                && wrapper.getBackpack().getItem() instanceof ImplantBackpackItem;
    }

    @Inject(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$noPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (spellclasses$implantBackpack()) cir.setReturnValue(false);
    }

    @Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$noPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (spellclasses$implantBackpack()) cir.setReturnValue(false);
    }
}
