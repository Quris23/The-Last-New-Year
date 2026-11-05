package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.AscendedOneMana;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spells of mana-limited bosses (see AscendedOneMana) are not started when the pool is too low, and no spellcasting mob
 * can start one while it carries the Silence effect (Discerning The Eldritch; e.g. from the Weighing of Souls).
 */
@Mixin(value = AbstractSpellCastingMob.class, remap = false)
public abstract class AscendedOneManaMixin {
    @Inject(method = "initiateCastSpell", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$manaGate(AbstractSpell spell, int level, CallbackInfo ci) {
        if (AscendedOneMana.isSilenced((AbstractSpellCastingMob) (Object) this)) {
            ci.cancel();
            return;
        }
        if (!AscendedOneMana.tryConsume((AbstractSpellCastingMob) (Object) this, spell, level)) ci.cancel();
    }
}
