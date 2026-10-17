package dev.qur1s.atlas;

import dev.qur1s.atlas.client.AtlasClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** The Atlas: right-click opens it (the key opens it from the Atlas curio slot). */
public class AtlasItem extends Item {
    public AtlasItem(Properties properties) {
        super(properties);
    }

    /** Gives a new Atlas its own id the first time it is carried, so its map data is separate from every other Atlas. */
    public static UUID ensureId(ItemStack stack) {
        UUID id = stack.get(AtlasComponents.ATLAS_ID.get());
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(AtlasComponents.ATLAS_ID.get(), id);
        }
        return id;
    }

    /**
     * A stack of unused Atlases shares no id: before one is used it is split off the stack, given its own id and put
     * in the inventory. Returns the stack to work with (the same one when it was a single Atlas already).
     */
    public static ItemStack unique(Player player, ItemStack stack) {
        if (stack.getCount() <= 1) return stack;
        ItemStack single = stack.split(1);
        ensureId(single);
        if (!player.getInventory().add(single)) player.drop(single, false);
        return single;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || level.getServer() == null) return;
        UUID id = ensureId(stack);
        if ((level.getGameTime() + slot) % 40 == 0) {
            int chunks = dev.qur1s.atlas.data.AtlasData.get(level.getServer()).chunkCount(id);
            Integer known = stack.get(AtlasComponents.ATLAS_CHUNKS.get());
            if (known == null || known != chunks) stack.set(AtlasComponents.ATLAS_CHUNKS.get(), chunks);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        Integer chunks = stack.get(AtlasComponents.ATLAS_CHUNKS.get());
        if (chunks == null || chunks < 0) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.atlas.empty").withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.atlas.coverage", chunks).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            AtlasClient.open(stack.get(AtlasComponents.ATLAS_ID.get()));
        } else {
            ensureId(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
