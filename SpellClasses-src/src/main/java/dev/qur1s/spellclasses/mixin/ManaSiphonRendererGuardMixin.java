package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.qur1s.spellclasses.ManaSiphonGuard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips Create: Wizardry's Mana Siphon renderer while its partial models are not baked (see ManaSiphonGuard). */
@Pseudo
@Mixin(targets = "net.ttzplayz.create_wizardry.client.rendering.ManaSiphonRenderer", remap = false)
public abstract class ManaSiphonRendererGuardMixin {
    @Inject(method = "renderSafe(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$guard(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (!ManaSiphonGuard.ready()) ci.cancel();
    }
}
