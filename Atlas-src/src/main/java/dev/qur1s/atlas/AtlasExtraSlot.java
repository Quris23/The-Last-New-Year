package dev.qur1s.atlas;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** The cartography table's second slot (paper, empty map, glass pane), which also takes an Atlas. */
public class AtlasExtraSlot extends Slot {
    public AtlasExtraSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Items.PAPER) || stack.is(Items.MAP) || stack.is(Items.GLASS_PANE) || stack.is(AtlasItems.ATLAS.get());
    }
}
