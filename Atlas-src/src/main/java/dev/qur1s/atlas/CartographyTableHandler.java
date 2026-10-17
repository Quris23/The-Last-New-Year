package dev.qur1s.atlas;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Pouring a map into an Atlas, two ways:
 * - the Atlas in one hand and a filled map in the other, right-click (on a block or in the air);
 * - Shift + right-click with a filled map in hand while the Atlas is in its Artifacts slot.
 * Everything the map has drawn goes into that Atlas, and the map is used up.
 * Copying an Atlas happens in the cartography table's window (see CartographyTableMenuMixin).
 */
@EventBusSubscriber(modid = "atlas")
public final class CartographyTableHandler {
    private CartographyTableHandler() {
    }

    /** Handles the click if it is a pour; returns whether it was. */
    private static boolean tryPour(Player player, boolean clientSide) {
        ItemStack main = player.getMainHandItem(), off = player.getOffhandItem();
        ItemStack atlas = main.is(AtlasItems.ATLAS.get()) ? main : off.is(AtlasItems.ATLAS.get()) ? off : ItemStack.EMPTY;
        ItemStack map = main.is(Items.FILLED_MAP) ? main : off.is(Items.FILLED_MAP) ? off : ItemStack.EMPTY;
        if (!atlas.isEmpty() && !map.isEmpty()) {
            if (!clientSide && player instanceof ServerPlayer sp) AtlasActions.pourMap(sp, atlas, map);
            return true;
        }
        if (atlas.isEmpty() || map.isEmpty()) {
            // Shift + right-click with a map, Atlas in the Artifacts slot
            if (player.isShiftKeyDown() && main.is(Items.FILLED_MAP)) {
                ItemStack equipped = Atlases.equippedStack(player);
                if (equipped.isEmpty()) return false;
                if (!clientSide && player instanceof ServerPlayer sp) AtlasActions.pourMap(sp, equipped, main);
                return true;
            }
            return false;
        }
        return true;
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND && event.getHand() != InteractionHand.OFF_HAND) return;
        // the lectern has its own handling for an Atlas standing on it
        if (event.getLevel().getBlockState(event.getPos()).is(net.minecraft.world.level.block.Blocks.LECTERN)
                && !event.getEntity().getMainHandItem().is(Items.FILLED_MAP)) return;
        if (tryPour(event.getEntity(), event.getLevel().isClientSide)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (tryPour(event.getEntity(), event.getLevel().isClientSide)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
