package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.client.BloodLayering;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Marks the span in which the first-person hands are drawn (see BloodLayering). */
@Mixin(GameRenderer.class)
public abstract class GameRendererHandFlagMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), require = 0)
    private void spellclasses$handsBegin(CallbackInfo ci) {
        BloodLayering.handsActive = true;
    }

    @Inject(method = "renderItemInHand", at = @At("RETURN"), require = 0)
    private void spellclasses$handsEnd(CallbackInfo ci) {
        BloodLayering.handsActive = false;
    }
}
