package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.config.ModifyDefaultConfigValuesEvent;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.spells.ender.BlackHoleSpell;
import io.redspace.ironsspellbooks.spells.ender.CounterspellSpell;
import io.redspace.ironsspellbooks.spells.blood.WitherSkullSpell;
import io.redspace.ironsspellbooks.spells.ender.StarfallSpell;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Echoing Magic moves Iron's Black Hole and Starfall into Hazen's "Cosmic" school when the default spell configs are built
 * ({@code EMSpellEvents.modifySpellSchool}). They are Ender spells in this pack, so they are put back right after, at the
 * lowest priority so that this handler runs after Echoing Magic's. Wither Skull, which Echoing Magic moves into the cut Shadow
 * school, goes back to Blood. Counterspell is moved into the Evocation school. The Cosmic school's own spells (Echoing Magic) stay as they are.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class EnderSpellSchoolRestore {
    private EnderSpellSchoolRestore() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void restoreEnder(ModifyDefaultConfigValuesEvent event) {
        var spell = event.getSpell();
        if (spell instanceof BlackHoleSpell || spell instanceof StarfallSpell) {
            event.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.ENDER.get());
        }
        if (spell instanceof CounterspellSpell) {
            // Counterspell belongs to the Evocation (Призыв) school
            event.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.EVOCATION.get());
        }
        if (spell instanceof WitherSkullSpell) {
            event.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.BLOOD.get());
        }
    }
}
