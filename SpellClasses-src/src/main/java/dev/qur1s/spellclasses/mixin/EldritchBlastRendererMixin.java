package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.spells.eldritch_blast.EldritchBlastRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The Zoltraak beam is drawn twice as wide (its length stays as it was). */
@Mixin(value = EldritchBlastRenderer.class, remap = false)
public abstract class EldritchBlastRendererMixin {
    private static final float WIDTH = 2.0f;

    @Redirect(method = "render(Lio/redspace/ironsspellbooks/entity/spells/eldritch_blast/EldritchBlastVisualEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 0), require = 0)
    private void spellclasses$wideBeam(PoseStack stack, float x, float y, float z) {
        stack.scale(x * WIDTH, y, z * WIDTH);
    }
}
