package dev.qur1s.spellclasses;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The tentacle rig lives inside a Chromeware implant attachment, which hands out copies of its stack: whatever spends the
 * rig's air changes a copy. Every tick the change is written back and synced (see {@link OctoRigBackSlot#flush}). A rig that
 * still ends up in the chest slot (a stack saved before, a dispenser) is handed back: it is an implant only.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class RigImplantSync {
    private RigImplantSync() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        OctoRigBackSlot.flush(player);
        if (player.tickCount % 10 != 0) return;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (OctoRigBackSlot.isTentacleRig(chest)) {
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            if (!player.getInventory().add(chest)) player.drop(chest, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        OctoRigBackSlot.forget(event.getEntity().getUUID());
    }
}
