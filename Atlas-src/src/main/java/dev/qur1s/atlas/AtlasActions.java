package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasMarker;
import dev.qur1s.atlas.network.AtlasNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.UUID;

/** The two things that can be done to an Atlas on the server: pour a map into it, put a banner's flag on it. */
public final class AtlasActions {
    private AtlasActions() {
    }

    /** Pours the filled map in {@code map} into {@code atlas} and uses the map up (outside creative). */
    public static void pourMap(ServerPlayer player, ItemStack atlas, ItemStack map) {
        MapItemSavedData data = MapItem.getSavedData(map, player.level());
        if (data == null) {
            player.displayClientMessage(Component.translatable("message.atlas.map_unreadable"), true);
            return;
        }
        atlas = AtlasItem.unique(player, atlas);
        UUID id = AtlasItem.ensureId(atlas);
        int changed = AtlasData.get(player.getServer()).merge(id, data);
        AtlasScan.enqueue(player, id, data);
        if (!player.getAbilities().instabuild) {
            map.shrink(1);
        }
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.atlas.map_added", changed), true);
    }

    /** Copies {@code atlas} into a new Atlas with its own id, using up the empty map {@code blank}. */
    public static void copyAtlas(ServerPlayer player, ItemStack atlas, ItemStack blank) {
        UUID from = atlas.get(AtlasComponents.ATLAS_ID.get());
        ItemStack copy = new ItemStack(AtlasItems.ATLAS.get());
        UUID to = AtlasItem.ensureId(copy);
        if (from != null) AtlasData.get(player.getServer()).mergeAtlas(from, to);
        if (!player.getAbilities().instabuild) blank.shrink(1);
        if (!player.getInventory().add(copy)) player.drop(copy, false);
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.atlas.copied"), true);
    }

    /** Puts a flag for the banner at {@code pos} on {@code atlas}. */
    public static void addFlag(ServerPlayer player, ItemStack atlas, BlockPos pos, AbstractBannerBlock banner) {
        Level level = player.level();
        String name = "";
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity entity && entity.hasCustomName()) {
            name = entity.getCustomName().getString();
        }
        UUID id = AtlasItem.ensureId(AtlasItem.unique(player, atlas));
        String dimension = level.dimension().location().toString();
        AtlasMarker marker = AtlasMarker.flag(UUID.randomUUID(), pos.getX(), pos.getY(), pos.getZ(),
                banner.getColor().getId(), name);
        if (AtlasData.get(player.getServer()).addMarker(id, dimension, marker)) {
            AtlasNetwork.sendMarkers(player, id, dimension);
            player.displayClientMessage(Component.translatable("message.atlas.marker_added"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.atlas.marker_exists"), true);
        }
    }
}
