package dev.qur1s.reviveraltar.mixin.client;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes ghost players render translucent with a custom ghost skin, by swapping out the
 * RenderType (and therefore texture) LivingEntityRenderer picks for them.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class GhostRenderTypeMixin {
    private static final ResourceLocation GHOST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReviveAltarMod.MODID, "textures/entity/ghost.png");

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void reviveraltar$ghostRenderType(LivingEntity entity, boolean bodyVisible, boolean translucent,
            boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (entity instanceof Player player && RevivalManager.isGhost(player)) {
            cir.setReturnValue(RenderType.entityTranslucent(GHOST_TEXTURE));
        }
    }
}
