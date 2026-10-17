package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * The cartography table's result slot. Taking a copied Atlas uses up the empty map only (the original stays in the
 * map slot) and gives the copy the original's terrain and markers; everything else behaves as in the game.
 */
public class AtlasCopyResultSlot extends Slot {
    private final Container input;
    private final ContainerLevelAccess access;

    public AtlasCopyResultSlot(Container input, Container result, int index, int x, int y, ContainerLevelAccess access) {
        super(result, index, x, y);
        this.input = input;
        this.access = access;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        ItemStack source = input.getItem(0);
        if (source.is(AtlasItems.ATLAS.get()) && stack.is(net.minecraft.world.item.Items.FILLED_MAP) && input.getItem(1).isEmpty()) {
            // the last poured map is taken out of the Atlas: it leaves the Atlas, nothing is used up
            var mapId = stack.get(net.minecraft.core.component.DataComponents.MAP_ID);
            UUID atlasId = source.get(AtlasComponents.ATLAS_ID.get());
            if (!player.level().isClientSide && player.getServer() != null && mapId != null && atlasId != null) {
                AtlasData.get(player.getServer()).removeMapSheet(atlasId, mapId.id());
            }
            access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                    SoundSource.BLOCKS, 1.0F, 1.0F));
            super.onTake(player, stack);
            return;
        }
        if (source.is(AtlasItems.ATLAS.get()) && stack.is(AtlasItems.ATLAS.get())) {
            if (!player.level().isClientSide && player.getServer() != null) {
                UUID from = source.get(AtlasComponents.ATLAS_ID.get());
                UUID to = stack.get(AtlasComponents.ATLAS_ID.get());
                if (from != null && to != null) AtlasData.get(player.getServer()).mergeAtlas(from, to);
                ItemStack second = input.getItem(1);
                UUID other = second.is(AtlasItems.ATLAS.get()) ? second.get(AtlasComponents.ATLAS_ID.get()) : null;
                if (other != null && to != null) AtlasData.get(player.getServer()).mergeAtlas(other, to);
            }
            input.removeItem(1, 1);
        } else {
            input.removeItem(0, 1);
            input.removeItem(1, 1);
        }
        stack.getItem().onCraftedBy(stack, player.level(), player);
        access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                SoundSource.BLOCKS, 1.0F, 1.0F));
        super.onTake(player, stack);
    }
}
