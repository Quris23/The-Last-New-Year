package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The Void Staff (cataclysm_spellbooks) is a plain weapon with no spell of its own - unlike the
 * Hither-Thither Wand, it doesn't implement {@code IPresetSpellContainer}, so it never gets a
 * {@code spell_container} component. This bakes two fixed locked spells into any Void Staff found on
 * a player - Portal (slot 0, matching the wand - see {@link GrimoireSchoolLockEvents} sibling-style
 * event and {@link PortalCastGateEvents} for the matching restriction, Portal only castable while
 * holding the wand or this staff) and, by user design, Counterspell (slot 1).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class VoidStaffPortalSpell {
    private VoidStaffPortalSpell() {
    }

    private static final ResourceLocation VOID_STAFF = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "void_staff");
    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final int COUNTERSPELL_LEVEL = 1;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        for (ItemStack stack : player.getInventory().items) {
            ensurePortalSpell(stack);
        }
        ensurePortalSpell(player.getOffhandItem());
    }

    private static void ensurePortalSpell(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(BuiltInRegistries.ITEM.get(VOID_STAFF))) return;
        if (ISpellContainer.isSpellContainer(stack)) {
            var container = ISpellContainer.get(stack);
            if (container.getMaxSpellCount() >= 2
                    && container.getSpellAtIndex(0).getSpell() == SpellRegistry.PORTAL_SPELL.get()
                    && container.getSpellAtIndex(1).getSpell() == SpellRegistry.COUNTERSPELL_SPELL.get()) {
                return;
            }
        }

        ISpellContainerMutable container = ISpellContainer.create(2, true, false).mutableCopy();
        container.addSpell(SpellRegistry.PORTAL_SPELL.get(), 1, true);
        container.addSpell(SpellRegistry.COUNTERSPELL_SPELL.get(), COUNTERSPELL_LEVEL, true);
        ISpellContainer.set(stack, container.toImmutable());
    }
}
