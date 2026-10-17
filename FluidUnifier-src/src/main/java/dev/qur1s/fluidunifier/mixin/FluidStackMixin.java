package dev.qur1s.fluidunifier.mixin;

import dev.qur1s.fluidunifier.Canon;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidStack.class, remap = false)
public abstract class FluidStackMixin {
    @Shadow @Final @Mutable
    private Fluid fluid;

    /** Every public constructor funnels into this private one. */
    @Inject(method = "<init>(Lnet/minecraft/world/level/material/Fluid;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("RETURN"))
    private void fluidunifier$canonicalize(Fluid fluid, int amount,
            net.minecraft.core.component.PatchedDataComponentMap components, CallbackInfo ci) {
        this.fluid = Canon.of(this.fluid);
    }

    @ModifyVariable(method = "is(Lnet/minecraft/world/level/material/Fluid;)Z", at = @At("HEAD"), argsOnly = true)
    private Fluid fluidunifier$canonicalizeIs(Fluid other) {
        return Canon.of(other);
    }
}
