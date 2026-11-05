package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cinematic's anvil scene assumes the vanilla {@link AnvilMenu}. For the other anvil-like menus (see CinematicAnvils) the three
 * places where it casts to it are answered without the cast: no experience cost, no renaming, the result click by container id.
 */
@Mixin(targets = "dev.dyslexic.cinematic.client.AnvilStation", remap = false)
public abstract class CinematicAnvilStationMixin {
    @Shadow
    private boolean toInventory;

    /** Cinematic only runs a scene whose kind is the block's: for these blocks that is the "addon" kind. */
    @Inject(method = "kind", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$addonKind(CallbackInfoReturnable<dev.dyslexic.cinematic.station.StationKind> cir) {
        if (((MenuStationAccessor) (Object) this).spellclasses$menuType() != AnvilMenu.class) {
            cir.setReturnValue(dev.dyslexic.cinematic.station.StationKind.ADDON);
        }
    }

    private boolean spellclasses$foreign() {
        return ((MenuStationAccessor) (Object) this).spellclasses$menuType() != AnvilMenu.class;
    }

    @Inject(method = "cost", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$noCost(CallbackInfoReturnable<Integer> cir) {
        if (spellclasses$foreign()) cir.setReturnValue(0);
    }

    /** There is no experience to pay at these anvils: the result is there to take or it is not. */
    @Inject(method = "affordable", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$free(CallbackInfoReturnable<Boolean> cir) {
        if (spellclasses$foreign()) cir.setReturnValue(!((MenuStationAccessor) (Object) this).spellclasses$slot(2).isEmpty());
    }

    @Inject(method = "sendName", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$noRename(String name, CallbackInfoReturnable<Boolean> cir) {
        if (spellclasses$foreign()) cir.setReturnValue(false);
    }

    @Inject(method = "lambda$forge$0", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$takeResult(CallbackInfo ci) {
        if (!spellclasses$foreign()) return;
        ci.cancel();
        Minecraft minecraft = Minecraft.getInstance();
        AbstractContainerMenu menu = ((MenuStationAccessor) (Object) this).spellclasses$menu();
        if (((MenuStationAccessor) (Object) this).spellclasses$slot(2).isEmpty()) {
            ((MenuStationAccessor) (Object) this).spellclasses$cancelMove();
            return;
        }
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, 2, 0,
                toInventory ? ClickType.QUICK_MOVE : ClickType.PICKUP, minecraft.player);
    }
}
