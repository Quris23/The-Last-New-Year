package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * These spells are each baked permanently into one specific preset weapon (see
 * PresetWeaponSpells, VoidStaffPortalSpell, and the vanilla items' own built-in preset spells:
 * Spellbreaker/Amethyst Rapier/Boreal Blade/Twilight Gale/Hellrazor/Monstrous Flamberge/
 * Hither-Thither Wand/Bloom Stone Staff/Soul Brazier). Their scrolls are hidden from the creative menu so nobody can put the
 * spell into a regular grimoire - the weapon is the only source. Scroll Forge crafting and JEI's
 * recipe listing for them are separately disabled via the "allow_crafting" spell config overlay
 * (kubejs/data/<mod_id>/irons_spellbooks_spell_config/<spell_id>.json); JEI's own item list
 * mirrors the creative tab, so hiding it here also hides it there.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class ExclusiveSpellScrollHider {
    private ExclusiveSpellScrollHider() {
    }

    private static final List<Supplier<AbstractSpell>> HIDDEN_SPELLS = List.of(
            SpellRegistry.VOLT_STRIKE_SPELL,
            SpellRegistry.ARCANE_SHACKLE_SPELL,
            SpellRegistry.COUNTERSPELL_SPELL,
            SpellRegistry.ECHOING_STRIKES_SPELL,
            SpellRegistry.FROSTBITE_SPELL,
            SpellRegistry.RAISE_HELL_SPELL,
            SpellRegistry.PORTAL_SPELL,
            SpellRegistries.TECTONIC_TREMBLE,
            SpellRegistries.CONJURE_AMETHYST_CRAB,
            SpellRegistries.CONJURE_THRALL
    );

    /** Cosmic school spells (Echoing Magic) cut from the pack; no compile dependency, so by id. */
    private static final List<ResourceLocation> HIDDEN_SPELL_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echo_star"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echo_blast"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echoing_explosion"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "echoing_slam"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "collapse"),
            ResourceLocation.fromNamespaceAndPath("echoing_magic", "summon_brave"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "iron_slash"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "aeropic"),
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ascension"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "tailwind"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "wind_blade"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "almighty_push"),
            ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "tornado")
    );

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        Set<ResourceLocation> hiddenIds = new LinkedHashSet<>();
        for (Supplier<AbstractSpell> spell : HIDDEN_SPELLS) {
            hiddenIds.add(spell.get().getSpellResource());
        }

        hiddenIds.addAll(HIDDEN_SPELL_IDS);

        Set<ItemStack> toRemove = new LinkedHashSet<>();
        for (ItemStack stack : event.getSearchEntries()) {
            if (isHiddenScroll(stack, hiddenIds)) toRemove.add(stack);
        }
        for (ItemStack stack : event.getParentEntries()) {
            if (isHiddenScroll(stack, hiddenIds)) toRemove.add(stack);
        }

        for (ItemStack stack : toRemove) {
            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean isHiddenScroll(ItemStack stack, Set<ResourceLocation> hiddenIds) {
        if (!stack.is(ItemRegistry.SCROLL.get())) return false;
        if (!ISpellContainer.isSpellContainer(stack)) return false;
        ISpellContainer container = ISpellContainer.get(stack);
        if (container.isEmpty()) return false;
        return hiddenIds.contains(container.getSpellAtIndex(0).getSpell().getSpellResource());
    }
}
