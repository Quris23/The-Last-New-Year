package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * "Aeronautics: Winds & Weather" draws its own first-person hands on the glider handles, while Punchy already draws
 * the player's arms - so in flight the hands were visible twice. Winds' hand draws are skipped (its glider handles are
 * still drawn; only the extra arms go).
 */
@Mixin(targets = "com.enzonic.aeronauticswinds.client.WindRiderGliderLayer", remap = false)
public abstract class WindsGliderHandsMixin {
    @Redirect(method = "renderFirstPersonHandles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
            remap = false), require = 0)
    private static void spellclasses$skipLeftHand(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffer,
                                                  int light, AbstractClientPlayer player) {
    }

    @Redirect(method = "renderFirstPersonHandles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
            remap = false), require = 0)
    private static void spellclasses$skipRightHand(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffer,
                                                   int light, AbstractClientPlayer player) {
    }
}
