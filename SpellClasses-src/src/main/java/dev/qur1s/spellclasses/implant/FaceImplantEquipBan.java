package dev.qur1s.spellclasses.implant;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

/**
 * The Hazmat Mask and the Night Vision Goggles are implants only: they cannot be put on in the head slot or in a Curios / Artifacts
 * slot. A right-click does nothing, Curios refuses them, and one that still ends up in the head slot is handed back.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class FaceImplantEquipBan {
    private FaceImplantEquipBan() {
    }

    private static boolean banned(ItemStack stack) {
        return !stack.isEmpty() && (FaceImplants.isMask(stack) || FaceImplants.isGoggles(stack));
    }

    @SubscribeEvent
    static void onCurioCanEquip(CurioCanEquipEvent event) {
        if (banned(event.getStack())) event.setEquipResult(TriState.FALSE);
    }

    @SubscribeEvent
    static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (banned(event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!banned(head)) return;
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        if (!player.getInventory().add(head)) player.drop(head, false);
    }
}
