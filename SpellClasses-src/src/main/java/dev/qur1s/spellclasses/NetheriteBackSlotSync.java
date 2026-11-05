package dev.qur1s.spellclasses;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Lava immunity of Create's netherite diving gear: the chest bit now means a netherite chestplate in the chest slot
 * together with a netherite backtank (or tentacle rig) with air in the Curios back slot. Create updates its bits only on
 * equipment change events, which neither the back slot nor this combination fires, so keep the chest bit in sync here.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class NetheriteBackSlotSync {
    private NetheriteBackSlotSync() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 4 != 0) return;
        if (!ModList.get().isLoaded("create")) return;
        ItemStack back = OctoRigBackSlot.stackFor(player, EquipmentSlot.CHEST, ItemStack.EMPTY);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean netheriteBack = OctoRigBackSlot.isRig(back)
                && BuiltInRegistries.ITEM.getKey(back.getItem()).getPath().startsWith("netherite")
                && BacktankUtil.hasAirRemaining(back);
        boolean want = netheriteBack && !chest.isEmpty() && !OctoRigBackSlot.isRig(chest)
                && NetheriteDivingHandler.isNetheriteArmor(chest);
        boolean has = (player.getPersistentData().getByte(NetheriteDivingHandler.NETHERITE_DIVING_BITS_KEY)
                & (1 << EquipmentSlot.CHEST.getIndex())) != 0;
        if (want && !has) NetheriteDivingHandler.setBit(player, EquipmentSlot.CHEST);
        else if (!want && has) NetheriteDivingHandler.clearBit(player, EquipmentSlot.CHEST);
    }
}
