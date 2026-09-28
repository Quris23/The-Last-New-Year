package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
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

    // Brontes: Twilight Gale's own spell (Volt Strike). Void Forge: Arcane Shackle 10.
    private static final Preset[] PRESETS = {
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm", "brontes"), SpellRegistry.VOLT_STRIKE_SPELL, 5),
            new Preset(ResourceLocation.fromNamespaceAndPath("cataclysm", "void_forge"), SpellRegistry.ARCANE_SHACKLE_SPELL, 10)
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
            if (ISpellContainer.isSpellContainer(stack)
                    && ISpellContainer.get(stack).getSpellAtIndex(0).getSpell() == expected) {
                return;
            }

            ISpellContainerMutable container = ISpellContainer.create(1, true, false).mutableCopy();
            container.addSpell(expected, preset.level(), true);
            ISpellContainer.set(stack, container.toImmutable());
            return;
        }
    }
}
