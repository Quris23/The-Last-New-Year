package dev.qur1s.atlas;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Putting a flag on an Atlas: right-click a banner with the Atlas in hand, or Shift + right-click it while the Atlas
 * is in its Artifacts slot.
 */
@EventBusSubscriber(modid = "atlas")
public final class BannerMarkerHandler {
    private BannerMarkerHandler() {
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        var level = event.getLevel();
        if (!(level.getBlockState(event.getPos()).getBlock() instanceof AbstractBannerBlock banner)) return;

        var player = event.getEntity();
        ItemStack atlas = ItemStack.EMPTY;
        if (player.getMainHandItem().is(AtlasItems.ATLAS.get())) {
            atlas = player.getMainHandItem();
        } else if (player.isShiftKeyDown()) {
            atlas = Atlases.equippedStack(player);
        }
        if (atlas.isEmpty()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            AtlasActions.addFlag(sp, atlas, event.getPos(), banner);
        }
    }
}
