package dev.qur1s.spellclasses;

import com.momosoftworks.coldsweat.api.temperature.modifier.SimpleTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

/**
 * The Frostward Ring's own {@code -11.2C Freezing Point} bonus comes from a vanilla attribute
 * modifier with a fixed id tied to the item type - confirmed via {@code /temp debug}: wearing two
 * rings still only shows ONE {@code cs:irons_frostward_protection} entry, since vanilla's attribute
 * map is keyed by modifier id and a second modifier with the same id never stacks. This grants the
 * same per-ring value again for every ring beyond the first (up to the 4 curio ring slots).
 * <p>
 * Uses the plain {@link SimpleTempModifier} directly (as {@link ColdSweatHooks} already does for the
 * Ice class's full resistance grant) rather than a custom subclass - Cold Sweat serializes its
 * modifier list through a codec keyed by concrete class, and an unregistered subclass has no codec
 * entry, which threw a NullPointerException out of {@code AbstractTempCap.serializeModifiers} and
 * crashed the server the moment a second ring got equipped. {@code FREEZING_POINT} isn't touched by
 * any other {@code SimpleTempModifier} in this setup, so removing all of them on that trait before
 * re-adding ours is safe (mirrors the same removeModifiers(..., SimpleTempModifier.class) pattern).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class FrostwardRingStacking {
    private FrostwardRingStacking() {
    }

    /** Matches the ring's own tooltip value (-11.2C = -0.448 MC units). */
    private static final double PER_RING_VALUE = -0.448;
    private static final int CHECK_INTERVAL_TICKS = 20;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        int ringCount = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findCurios(ItemRegistry.FROSTWARD_RING.get()))
                .map(FrostwardRingStacking::totalCount)
                .orElse(0);

        int extraRings = Math.max(0, ringCount - 1);
        Temperature.removeModifiers(player, Temperature.Trait.FREEZING_POINT, SimpleTempModifier.class);
        if (extraRings > 0) {
            Temperature.addModifier(player, new SimpleTempModifier(extraRings * PER_RING_VALUE, SimpleTempModifier.Operation.ADD),
                    Temperature.Trait.FREEZING_POINT, Placement.LAST);
        }
    }

    private static int totalCount(List<SlotResult> results) {
        int total = 0;
        for (SlotResult result : results) {
            total += result.stack().getCount();
        }
        return total;
    }
}
