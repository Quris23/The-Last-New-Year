package dev.qur1s.sphereshields.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public interface ContainerScreenAccessor {
    @Accessor("leftPos")
    int sphereshields$getLeftPos();

    @Accessor("topPos")
    int sphereshields$getTopPos();

    @Accessor("imageWidth")
    int sphereshields$getImageWidth();
}
