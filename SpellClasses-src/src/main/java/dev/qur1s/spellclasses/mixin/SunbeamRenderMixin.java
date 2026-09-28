package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * SunbeamRenderer's charge-up animation (beam width + brightness) is driven by its own hardcoded
 * 15-tick curve and a hardcoded max visual radius, completely independent of the entity's actual
 * warmup timing / hit radius (which {@link SunbeamSizeMixin} doubles). Match the visuals to it:
 * the animation now plays out over 30 ticks instead of 15, and the beam is twice as wide.
 */
@Mixin(value = SunbeamRenderer.class, remap = false)
public abstract class SunbeamRenderMixin {
    @ModifyConstant(
            method = "render(Lio/redspace/ironsspellbooks/entity/spells/sunbeam/SunbeamEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            constant = @Constant(floatValue = 15.0f))
    private float spellclasses$slowAnimation(float original) {
        return 30.0f;
    }

    @ModifyConstant(
            method = "render(Lio/redspace/ironsspellbooks/entity/spells/sunbeam/SunbeamEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            constant = @Constant(floatValue = 2.5f))
    private float spellclasses$widenBeam(float original) {
        return 5.0f;
    }
}
