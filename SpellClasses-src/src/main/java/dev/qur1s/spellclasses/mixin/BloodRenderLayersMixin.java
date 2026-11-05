package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.client.BloodLayering;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The render types Hemodynamics uses for blood on bodies and armor are drawn a little closer to the camera. */
@Pseudo
@Mixin(targets = "breenihilation.hemodynamics.client.render.BloodRenderLayers", remap = false)
public abstract class BloodRenderLayersMixin {
    @Redirect(method = {"bodyGloss", "bodyStains", "armorStains", "armorGloss"}, remap = false, require = 0,
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false,
                    target = "Lbreenihilation/hemodynamics/client/render/BloodRenderLayers;VIEW_OFFSET_Z_LAYERING:Lnet/minecraft/client/renderer/RenderStateShard$LayeringStateShard;"))
    private static RenderStateShard.LayeringStateShard spellclasses$nearer() {
        return BloodLayering.NEAR;
    }

    /** The skin atlas layers (arms, head...) test the depth for equality: any other layer on the same surface made them flicker. */
    @Redirect(method = {"exactStains", "exactGloss"}, remap = false, require = 0,
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false,
                    target = "Lbreenihilation/hemodynamics/client/render/BloodRenderLayers;EQUAL_DEPTH_TEST:Lnet/minecraft/client/renderer/RenderStateShard$DepthTestStateShard;"))
    private static RenderStateShard.DepthTestStateShard spellclasses$lequal() {
        return BloodLayering.ADAPTIVE;
    }

    @Redirect(method = {"exactStains", "exactGloss"}, remap = false, require = 0,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setWriteMaskState(Lnet/minecraft/client/renderer/RenderStateShard$WriteMaskStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"))
    private static RenderType.CompositeState.CompositeStateBuilder spellclasses$near(
            RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard.WriteMaskStateShard mask) {
        return builder.setWriteMaskState(mask).setLayeringState(BloodLayering.NEAR_EXACT);
    }
}
