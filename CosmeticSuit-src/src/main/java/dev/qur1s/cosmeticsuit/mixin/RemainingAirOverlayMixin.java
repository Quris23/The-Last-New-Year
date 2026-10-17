package dev.qur1s.cosmeticsuit.mixin;

import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NorthStar already shows the oxygen timer for copper/netherite backtanks; drop Create's duplicate. */
@Mixin(value = RemainingAirOverlay.class, remap = false)
public class RemainingAirOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cosmeticsuit$hideDuplicate(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && CosmeticSuit.isOxygenBacktank(RemainingAirOverlay.getDisplayedBacktank(player))) ci.cancel();
    }
}
