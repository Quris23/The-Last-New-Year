package dev.qur1s.spellclasses;

import com.momosoftworks.coldsweat.api.temperature.modifier.SimpleTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import dev.qur1s.sphereshields.GroundShieldTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Central, per-player application of the Northstar Temperature Regulator -> Cold Sweat WORLD sync,
 * reading state registered by every regulator into {@link RegulatorSyncTracker}. Each player gets
 * exactly one {@code removeModifiers}/{@code addModifier} pair per check, picking the first regulator
 * (if any) whose working area contains them - either Northstar's own sealed room ({@code active &&
 * isSealed(pos)}) or a SphereShields ground dome that contains the regulator and fits its capacity
 * (see {@link NorthstarSphereShieldsSupport#fieldDomeIfWithinCapacity}).
 * <p>
 * Doing this here instead of inside each regulator's own tick avoids multiple regulators fighting over
 * the same player's modifier list (see {@link RegulatorSyncTracker}'s javadoc for the bug this fixes).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class NorthstarRegulatorColdSweatSync {
    private NorthstarRegulatorColdSweatSync() {
    }

    private static final int CHECK_INTERVAL_TICKS = 20;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Level level = player.level();
        Float matchedTemperature = null;

        for (RegulatorSyncTracker.RegulatorInfo info : RegulatorSyncTracker.regulatorsIn(level.dimension(), level.getGameTime())) {
            boolean sealedByRegulator = info.active() && info.regulator().isSealed(player.position());

            boolean sealedByField = false;
            if (!sealedByRegulator) {
                GroundShieldTracker.Dome fieldDome = NorthstarSphereShieldsSupport.fieldDomeIfWithinCapacity(info.regulator());
                sealedByField = fieldDome != null && GroundShieldTracker.contains(fieldDome, player.position());
            }

            if (sealedByRegulator || sealedByField) {
                matchedTemperature = info.temperature();
                break;
            }
        }

        Temperature.removeModifiers(player, Temperature.Trait.WORLD, SimpleTempModifier.class);
        if (matchedTemperature != null) {
            Temperature.addModifier(player, new SimpleTempModifier(matchedTemperature / 25.0, SimpleTempModifier.Operation.SET),
                    Temperature.Trait.WORLD, Placement.LAST);
        }
    }
}
