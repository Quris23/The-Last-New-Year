package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cinematic's cartography scene assumes the vanilla {@link CartographyTableMenu}. For the other three-slot menus (see
 * CinematicAnvils) the one place where it casts to it takes the result by container id instead.
 */
@Mixin(targets = "dev.dyslexic.cinematic.client.CartographyStation", remap = false)
public abstract class CinematicCartographyStationMixin {
    @Shadow
    private boolean toInventory;

    /** Cinematic only runs a scene whose kind is the block's: for these blocks that is the "addon" kind. */
    @Inject(method = "kind", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$addonKind(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<dev.dyslexic.cinematic.station.StationKind> cir) {
        if (((MenuStationAccessor) (Object) this).spellclasses$menuType() != CartographyTableMenu.class) {
            cir.setReturnValue(dev.dyslexic.cinematic.station.StationKind.ADDON);
        }
    }

    @Inject(method = "lambda$work$0", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$takeResult(Minecraft minecraft, CallbackInfo ci) {
        if (((MenuStationAccessor) (Object) this).spellclasses$menuType() == CartographyTableMenu.class) return;
        AbstractContainerMenu menu = ((MenuStationAccessor) (Object) this).spellclasses$menu();
        if (menu == null) return;
        ci.cancel();
        if (((MenuStationAccessor) (Object) this).spellclasses$slot(2).isEmpty()) {
            ((MenuStationAccessor) (Object) this).spellclasses$cancelMove();
            return;
        }
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, 2, 0,
                toInventory ? ClickType.QUICK_MOVE : ClickType.PICKUP, minecraft.player);
    }
}
