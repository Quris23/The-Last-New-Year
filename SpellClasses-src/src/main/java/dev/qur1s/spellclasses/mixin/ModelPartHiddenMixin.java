package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.qur1s.spellclasses.client.ChromewareHidden;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chromeware hides the parts of a missing limb or torso from its own ModelPart.render hook. 3D Skin Layers draws the
 * second skin layer from a hook of its own on the same method (and on Entity Model Features' parts) and cancels the
 * call before Chromeware gets to say "hidden", so the layer stayed floating where the body used to be. These hooks run
 * first (lower priority number) and cancel the call for the parts Chromeware hides.
 */
public final class ModelPartHiddenMixin {
    private ModelPartHiddenMixin() {
    }

    private static final String RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V";

    @Mixin(value = ModelPart.class, priority = 500)
    public abstract static class Vanilla {
        @Inject(method = RENDER, at = @At("HEAD"), cancellable = true, require = 0)
        private void spellclasses$hidden(PoseStack stack, VertexConsumer buffer, int light, int overlay, int color,
                                         CallbackInfo ci) {
            if (ChromewareHidden.isHidden((ModelPart) (Object) this)) ci.cancel();
        }
    }

    @Pseudo
    @Mixin(targets = "traben.entity_model_features.models.parts.EMFModelPart", priority = 500, remap = false)
    public abstract static class Emf {
        @Inject(method = RENDER, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
        private void spellclasses$hidden(PoseStack stack, VertexConsumer buffer, int light, int overlay, int color,
                                         CallbackInfo ci) {
            if (ChromewareHidden.isHidden((ModelPart) (Object) this)) ci.cancel();
        }
    }
}
