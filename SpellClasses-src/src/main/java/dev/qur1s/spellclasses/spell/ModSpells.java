package dev.qur1s.spellclasses.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModSpells {
    private ModSpells() {
    }

    private static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, "spellclasses");

    public static final Supplier<AbstractSpell> LIGHT_SHIELD = register(new LightShieldSpell());

    private static Supplier<AbstractSpell> register(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static void register(IEventBus modBus) {
        SPELLS.register(modBus);
    }
}
