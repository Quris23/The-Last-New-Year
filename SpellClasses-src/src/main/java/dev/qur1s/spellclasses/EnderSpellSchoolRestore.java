package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.config.ModifyDefaultConfigValuesEvent;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.spells.ender.BlackHoleSpell;
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
 * school, goes back to Blood. The Cosmic school's own spells (Echoing Magic) are switched off altogether.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class EnderSpellSchoolRestore {
    private EnderSpellSchoolRestore() {
    }

    private static final java.util.Set<ResourceLocation> COSMIC_SPELLS = java.util.Set.of(
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echo_star"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echo_blast"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echoing_explosion"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echoing_slam"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "collapse"));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void restoreEnder(ModifyDefaultConfigValuesEvent event) {
        var spell = event.getSpell();
        if (spell instanceof BlackHoleSpell || spell instanceof StarfallSpell) {
            event.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.ENDER.get());
        }
        if (spell instanceof WitherSkullSpell) {
            event.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.BLOOD.get());
        }
        if (COSMIC_SPELLS.contains(spell.getSpellResource())) {
            event.setDefaultValue(SpellConfigParameter.ENABLED, false);
        }
    }
}
