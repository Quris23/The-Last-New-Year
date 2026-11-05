package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Write access to a spell's spell-power curve (the fields belong to AbstractSpell, so a subclass mixin cannot shadow them). */
@Mixin(value = AbstractSpell.class, remap = false)
public interface AbstractSpellPowerAccessor {
    @Accessor("baseSpellPower")
    void spellclasses$setBaseSpellPower(int value);

    @Accessor("spellPowerPerLevel")
    void spellclasses$setSpellPowerPerLevel(int value);
}
