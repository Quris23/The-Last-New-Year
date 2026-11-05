package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.spells.eldritch.EldritchBlastSpell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Eldritch Blast (Потусторонний выстрел, now «Зольтрак»): damage grows with the spell level, 16 at level 1 to 20 at
 * level 5 (it was a flat 15). The mana cost is set to a flat 50 in AbstractSpellOverridesMixin.
 */
@Mixin(value = EldritchBlastSpell.class, remap = false)
public abstract class EldritchBlastMixin {
    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void spellclasses$damageCurve(CallbackInfo ci) {
        AbstractSpellPowerAccessor curve = (AbstractSpellPowerAccessor) (Object) this;
        curve.spellclasses$setBaseSpellPower(16);
        curve.spellclasses$setSpellPowerPerLevel(1);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$moreDamage(int spellLevel, net.minecraft.world.entity.LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 1.2f);
    }

    /** The beam's hit width (the ray is inflated by this much), doubled together with its drawing. */
    @ModifyConstant(method = "onCast", constant = @Constant(floatValue = 0.15f), require = 0)
    private float spellclasses$widerHit(float width) {
        return width * 2.0f;
    }
}
