package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.spells.blood.BloodSlashSpell;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Blood Slash: no more lifesteal. Mana cost is handled by {@link AbstractSpellManaCostMixin}. */
@org.spongepowered.asm.mixin.Mixin(value = BloodSlashSpell.class, remap = false)
public abstract class BloodSlashMixin {
    @ModifyArg(
            method = "getDamageSource",
            at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/damage/SpellDamageSource;setLifestealPercent(F)Lio/redspace/ironsspellbooks/damage/SpellDamageSource;"))
    private float spellclasses$noHeal(float percent) {
        return 0.0f;
    }
}
