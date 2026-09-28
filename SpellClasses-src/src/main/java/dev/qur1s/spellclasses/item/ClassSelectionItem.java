package dev.qur1s.spellclasses.item;

import dev.qur1s.spellclasses.client.ClientScreenOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClassSelectionItem extends Item {
    public ClassSelectionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            ClientScreenOpener.openClassSelection();
        }
        return InteractionResultHolder.success(stack);
    }
}
