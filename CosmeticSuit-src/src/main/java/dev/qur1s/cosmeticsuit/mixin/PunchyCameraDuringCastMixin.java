package dev.qur1s.cosmeticsuit.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Punchy moves the real first-person camera to follow a "camera" bone it finds by name in
 * whatever GeckoLib model last rendered (see {@code SimpleBedrockGeo.loadedParts}) — a global,
 * name-based lookup that isn't scoped to the model currently being posed. During Iron's
 * Spellbooks' cast animation this occasionally grabs the wrong/stale bone and yanks the camera
 * into the player's own head. Rather than touch that lookup, we just suppress the camera-follow
 * effect for the duration of a cast; the cast animation itself (arms/held item) is unaffected.
 */
@Mixin(targets = "punchy.client.render.CameraAnimationState", remap = false)
public class PunchyCameraDuringCastMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private static void cosmeticsuit$suppressDuringCast(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && MagicData.getPlayerMagicData(player).isCasting()) {
            cir.setReturnValue(false);
        }
    }
}
