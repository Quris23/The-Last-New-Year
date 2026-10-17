package dev.qur1s.reviveraltar.mixin.optional.client;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * "AppleStats" draws two unwanted, unconfigurable HUD overlays: a coordinates/facing-vector
 * readout (top-left) and a saturation number (near the hotbar). Only the coordinates one is
 * unwanted here; this redirects away its registration call (the second of the two
 * registerAbove calls in that method) so it's simply never added, leaving the saturation
 * overlay untouched. A soft/optional target - does nothing if AppleStats isn't installed.
 */
@Mixin(targets = "net.mcreator.applestat.OverlayHandler", remap = false)
public class DisableCoordsOverlayMixin {

    @Redirect(method = "OverlayHandler", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/client/event/RegisterGuiLayersEvent;registerAbove(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/gui/LayeredDraw$Layer;)V",
            ordinal = 1))
    private static void reviveraltar$skipCoordsOverlay(RegisterGuiLayersEvent event,
            ResourceLocation above, ResourceLocation id, LayeredDraw.Layer layer) {
        // Intentionally not registered.
    }
}
