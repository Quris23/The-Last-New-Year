package dev.qur1s.atlas;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The cartography table's map slot, which also takes an Atlas (to be copied). */
public class AtlasMapSlot extends Slot {
    public AtlasMapSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.has(DataComponents.MAP_ID) || stack.is(AtlasItems.ATLAS.get());
    }
}
