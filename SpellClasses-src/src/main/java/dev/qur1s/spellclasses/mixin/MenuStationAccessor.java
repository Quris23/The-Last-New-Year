package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "dev.dyslexic.cinematic.client.MenuStation", remap = false)
public interface MenuStationAccessor {
    @Mutable
    @Accessor(value = "menuType", remap = false)
    void spellclasses$setMenuType(Class<? extends AbstractContainerMenu> type);

    @Accessor(value = "menuType", remap = false)
    Class<? extends AbstractContainerMenu> spellclasses$menuType();

    @Invoker(value = "menu", remap = false)
    AbstractContainerMenu spellclasses$menu();

    @Invoker(value = "slot", remap = false)
    ItemStack spellclasses$slot(int slot);

    @Invoker(value = "cancelMove", remap = false)
    void spellclasses$cancelMove();
}
