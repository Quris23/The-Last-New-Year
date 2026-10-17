package dev.qur1s.fluidunifier.mixin;

import dev.qur1s.fluidunifier.Canon;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = net.minecraft.world.level.material.Fluid.class, remap = false)
public abstract class FluidMixin {
    @Inject(method = "isSame(Lnet/minecraft/world/level/material/Fluid;)Z", at = @At("HEAD"), cancellable = true)
    private void fluidunifier$isSameCanonical(Fluid other, CallbackInfoReturnable<Boolean> cir) {
        Fluid self = (Fluid) (Object) this;
        Fluid a = Canon.of(self);
        Fluid b = Canon.of(other);
        if (a != self || b != other) {
            cir.setReturnValue(a.isSame(b));
        }
    }
}
