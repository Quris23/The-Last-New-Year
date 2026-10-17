package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import com.anton.shieldgenerators.ShieldGeneratorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.qur1s.sphereshields.SignalHolder;
import dev.qur1s.sphereshields.client.HexCoreSpin;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The generator's block model is now the static Hex Core shell (see assets/shield_generators/blockstates);
 * its inner cube is drawn here, spinning, before the shield itself so the dome renders on top of it. The spin
 * speed follows the generator's redstone signal (see {@link HexCoreSpin}).
 */
@Mixin(value = ShieldGeneratorRenderer.class, remap = false)
abstract class HexCoreRenderMixin {
    @Inject(method = "render(Lcom/anton/shieldgenerators/ShieldGeneratorBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"))
    private void sphereshields$renderHexCore(ShieldGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        int signal = ((SignalHolder) (Object) blockEntity).sphereshields$getLastSignal();
        HexCoreSpin.render(blockEntity, signal, partialTick, poseStack, bufferSource);
    }
}
