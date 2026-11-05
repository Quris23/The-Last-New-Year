package dev.qur1s.spellclasses.implant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers who killed with a Chromeware killing quickhack, for the FTB Quests task (read from the KubeJS script by {@link #take}).
 * A creature counts as killed by a quickhack when it dies within {@link #WINDOW} ticks of being hit by one of the nine killing hacks
 * (so damage over time, a spread, a psychotic mob killed by others, or an explosion all count).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class QuickhackKills {
    public static final int WINDOW = 600;

    private record Hit(UUID player, long tick) {
    }

    private static final Map<UUID, Hit> HACKED = new ConcurrentHashMap<>();
    private static final Set<UUID> KILLERS = ConcurrentHashMap.newKeySet();

    private QuickhackKills() {
    }

    public static boolean killing(String hack) {
        return switch (hack) {
            case "SHORT_CIRCUIT", "OVERHEAT", "CONTAGION", "SYNAPSE_BURNOUT", "SYSTEM_COLLAPSE", "CYBERPSYCHOSIS", "SUICIDE",
                 "DETONATE_GRENADE", "BLACKWALL_GATEWAY" -> true;
            default -> false;
        };
    }

    public static void record(Player player, net.minecraft.world.entity.Entity target) {
        if (!(target instanceof LivingEntity) || player.level().isClientSide() || target == player) return;
        long now = player.level().getGameTime();
        HACKED.values().removeIf(h -> now - h.tick() > WINDOW * 2L);
        HACKED.put(target.getUUID(), new Hit(player.getUUID(), now));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        Hit hit = HACKED.remove(victim.getUUID());
        if (hit != null && victim.level().getGameTime() - hit.tick() <= WINDOW) KILLERS.add(hit.player());
    }

    /** True once, if this player killed something with a quickhack since the last call. */
    public static boolean take(Player player) {
        return KILLERS.remove(player.getUUID());
    }
}
