package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Hemodynamics wipes the blood of blades as soon as they are retracted (id -2 no longer matches the hand); it stays on the arm now. */
@Pseudo
@Mixin(targets = "breenihilation.hemodynamics.blood.BloodBodyStains", remap = false)
public abstract class HemodynamicsKeepBladeBloodMixin {
    @WrapOperation(method = "maintain", remap = false, require = 0,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lbreenihilation/hemodynamics/blood/BloodBodyStains;dropRemoved([I[I)Z"))
    private static boolean spellclasses$keepBlades(int[] recorded, int[] current, Operation<Boolean> original) {
        int[] seen = current.clone();
        for (int index = 0; index < seen.length; index++) {
            if (recorded[index] == -2) seen[index] = -2;
        }
        return original.call(recorded, seen);
    }
}
