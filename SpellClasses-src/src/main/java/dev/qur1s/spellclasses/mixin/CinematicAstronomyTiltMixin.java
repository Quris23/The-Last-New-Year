package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.qur1s.spellclasses.compat.AstronomyTilt;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The cartography scene, put on Northstar's tilted astronomy table: items sit on the slope, and the cursor hits the slope. */
public final class CinematicAstronomyTiltMixin {
    private CinematicAstronomyTiltMixin() {
    }

    @Mixin(targets = "dev.dyslexic.cinematic.client.CartographyStation", remap = false)
    public abstract static class OnStation {
        @Shadow
        @Final
        public Vec3 top;

        @Inject(method = "slotPoint", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$onBoard(Vec3 top, Direction side, int slot, CallbackInfoReturnable<Vec3> cir) {
            Direction facing = AstronomyTilt.facing(top);
            if (facing == null) return;
            Vec3 flat = cir.getReturnValue();
            cir.setReturnValue(new Vec3(flat.x, AstronomyTilt.boardY(top, facing, flat), flat.z));
        }

        @WrapOperation(method = "updateCursor", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Ldev/dyslexic/cinematic/client/Picking;cursorOnPlane(D)Lnet/minecraft/world/phys/Vec3;"))
        private Vec3 spellclasses$cursorOnBoard(double y, Operation<Vec3> original) {
            Direction facing = AstronomyTilt.facing(top);
            if (facing == null) return original.call(y);
            Vec3 middle = new Vec3(top.x, top.y - AstronomyTilt.DROP, top.z);
            return dev.dyslexic.cinematic.client.Picking.cursorOnPlane(middle, AstronomyTilt.normal(facing));
        }
    }

    @Mixin(targets = "dev.dyslexic.cinematic.client.ItemPlacer", remap = false)
    public abstract static class OnPlacer {
        @ModifyVariable(method = "drawOriented", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
        private static Quaternionf spellclasses$tilt(Quaternionf flat, @Local(argsOnly = true, ordinal = 0) Vec3 position) {
            Direction facing = AstronomyTilt.facing(position);
            return facing == null ? flat : AstronomyTilt.lay(flat, facing);
        }
    }
}
