package dev.qur1s.atlas;

import dev.qur1s.atlas.network.OpenAtlasPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An Atlas can stand on a lectern like a book. Right-clicking the lectern opens the Atlas screen (the player may read
 * and edit it while standing near); Shift + right-click with an empty hand takes the Atlas back.
 */
@EventBusSubscriber(modid = "atlas")
public final class AtlasLectern {
    private static final double REACH = 8.0;

    private record Grant(UUID atlasId, GlobalPos pos) {
    }

    private static final Map<UUID, Grant> GRANTS = new HashMap<>();

    private AtlasLectern() {
    }

    /** Whether this player is allowed to use the Atlas with this id because it stands on a lectern within reach. */
    public static boolean allows(Player player, UUID id) {
        Grant grant = GRANTS.get(player.getUUID());
        if (grant == null || !grant.atlasId().equals(id)) return false;
        if (!grant.pos().dimension().equals(player.level().dimension())) return false;
        BlockPos pos = grant.pos().pos();
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > REACH * REACH) return false;
        if (player.level().getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            return id.equals(lectern.getBook().get(AtlasComponents.ATLAS_ID.get()));
        }
        return false;
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.LECTERN) || !level.getBlockState(pos).getValue(LecternBlock.HAS_BOOK)) return;
        if (!(level.getBlockEntity(pos) instanceof LecternBlockEntity lectern)) return;
        ItemStack book = lectern.getBook();
        if (!book.is(AtlasItems.ATLAS.get())) return;
        Player player = event.getEntity();
        // a pour (Atlas and map in the hands) is handled elsewhere
        if (player.getMainHandItem().is(net.minecraft.world.item.Items.FILLED_MAP)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.FILLED_MAP)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return;

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            ItemStack taken = book.copy();
            lectern.clearContent();
            LecternBlock.resetBookState(player, level, pos, level.getBlockState(pos), false);
            if (!player.getInventory().add(taken)) player.drop(taken, false);
            GRANTS.remove(sp.getUUID());
            return;
        }
        UUID id = AtlasItem.ensureId(book);
        lectern.setChanged();
        GRANTS.put(sp.getUUID(), new Grant(id, GlobalPos.of(level.dimension(), pos)));
        PacketDistributor.sendToPlayer(sp, new OpenAtlasPayload(id));
    }
}
