package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Brontes and the Void Forge are plain Cataclysm weapons with no spell of their own - unlike
 * Twilight Gale (the item Brontes is now fused from instead of Astrape, see staff_tweaks.js), they
 * don't implement {@code IPresetSpellContainer}, so they never get a {@code spell_container}
 * component. This bakes in a fixed locked spell for each, matching the pattern in
 * {@link VoidStaffPortalSpell}.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class PresetWeaponSpells {
    private PresetWeaponSpells() {
    }

    private record Preset(ResourceLocation item, Supplier<AbstractSpell> spell, int level) {
    }

    // Brontes: Twilight Gale's own spell (Volt Strike) 10. Void Forge: Arcane Shackle 10.
    // Bloom Stone Staff: Conjure Amethyst Crab 2 (crab count = level, see comment below).
    // Soul Brazier: Conjure Undead Thralls 10.
    private static final Preset[] PRESETS = {
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm", "brontes"), SpellRegistry.VOLT_STRIKE_SPELL, 10),
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm", "void_forge"), SpellRegistry.ARCANE_SHACKLE_SPELL, 10),
            // Conjure Amethyst Crab's onCast loops "for (i < spellLevel)" spawning one crab per
            // iteration - crab count is spellLevel, 1:1 (confirmed by decompiling the spell). Its own
            // declared max level (1) only bounds normal spellbook levelling; addSpell() here sets the
            // level directly and isn't clamped to it. By user design: 2 crabs.
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "bloom_stone_staff"), SpellRegistries.CONJURE_AMETHYST_CRAB, 2),
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "soul_brazier"), SpellRegistries.CONJURE_THRALL, 10)
    };

    private static final int CHECK_INTERVAL_TICKS = 40;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        for (ItemStack stack : player.getInventory().items) {
            ensureSpell(stack);
        }
        ensureSpell(player.getOffhandItem());
    }

    private static void ensureSpell(ItemStack stack) {
        if (stack.isEmpty()) return;
        for (Preset preset : PRESETS) {
            if (!stack.is(BuiltInRegistries.ITEM.get(preset.item()))) continue;

            AbstractSpell expected = preset.spell().get();
            if (ISpellContainer.isSpellContainer(stack)) {
                var current = ISpellContainer.get(stack).getSpellAtIndex(0);
                if (current.getSpell() == expected && current.getLevel() == preset.level()) {
                    return;
                }
            }

            ISpellContainerMutable container = ISpellContainer.create(1, true, false).mutableCopy();
            container.addSpell(expected, preset.level(), true);
            ISpellContainer.set(stack, container.toImmutable());
            return;
        }
    }
}
