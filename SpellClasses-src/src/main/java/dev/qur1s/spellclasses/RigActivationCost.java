package dev.qur1s.spellclasses;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Octo Arms: every activation of the tentacle rig (the double jump that takes it from STOWED to ACTIVE) costs one unit
 * of the rig's air, taken from the rig installed as a Chromeware implant. The state is read from Octo Arms' own server
 * state by reflection, so nothing happens when the mod is absent.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class RigActivationCost {
    private static final Map<UUID, String> LAST = new ConcurrentHashMap<>();
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static Method stateOf;
    private static boolean broken;

    private RigActivationCost() {
    }

    private static String state(ServerPlayer player) {
        try {
            if (stateOf == null) {
                stateOf = Class.forName("dev.tentaworks.rig.RigServer").getMethod("state", ServerPlayer.class);
            }
            return stateOf.invoke(null, player) instanceof Enum<?> e ? e.name() : null;
        } catch (Throwable t) {
            broken = true;
            return null;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (broken || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ModList.get().isLoaded("tentaworks") || !ModList.get().isLoaded("create")) return;
        String now = state(player);
        if (now == null) return;
        String before = LAST.put(player.getUUID(), now);
        if (!"ACTIVE".equals(now) || !"STOWED".equals(before)) return;
        if (player.isCreative() || player.isSpectator()) return;
        ItemStack rig = OctoRigBackSlot.stackFor(player, EquipmentSlot.CHEST, ItemStack.EMPTY);
        if (!OctoRigBackSlot.isTentacleRig(rig) || !BacktankUtil.hasAirRemaining(rig)) return;
        BacktankUtil.consumeAir(player, rig, 1);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST.remove(event.getEntity().getUUID());
    }
}
