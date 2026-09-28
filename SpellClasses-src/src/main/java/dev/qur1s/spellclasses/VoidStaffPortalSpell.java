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
 * {@code spell_container} component. This bakes the Portal spell into any Void Staff found on a
 * player, locked, exactly like the wand does for itself - see
 * {@link GrimoireSchoolLockEvents} sibling-style event, and {@link PortalCastGateEvents} for the
 * matching restriction (Portal only castable while holding the wand or this staff).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class VoidStaffPortalSpell {
    private VoidStaffPortalSpell() {
    }

    private static final ResourceLocation VOID_STAFF = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "void_staff");
    private static final int CHECK_INTERVAL_TICKS = 40;

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
        if (ISpellContainer.isSpellContainer(stack)) return;

        ISpellContainerMutable container = ISpellContainer.create(1, true, false).mutableCopy();
        container.addSpell(SpellRegistry.PORTAL_SPELL.get(), 1, true);
        ISpellContainer.set(stack, container.toImmutable());
    }
}
