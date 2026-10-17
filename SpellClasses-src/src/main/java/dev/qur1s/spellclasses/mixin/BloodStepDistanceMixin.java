package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.ScarecrowArmor;
import io.redspace.ironsspellbooks.spells.blood.BloodStepSpell;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Blood Step (Кровавая Поступь) reaches 10 blocks further for a caster in the full Scarecrow armor set. */
@Mixin(value = BloodStepSpell.class, remap = false)
public abstract class BloodStepDistanceMixin {
    @Inject(method = "getDistance", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$scarecrowBonus(int spellLevel, LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        if (caster != null && ScarecrowArmor.wearsFullSet(caster)) {
            cir.setReturnValue(cir.getReturnValue() + ScarecrowArmor.BLOOD_STEP_BONUS);
        }
    }
}
