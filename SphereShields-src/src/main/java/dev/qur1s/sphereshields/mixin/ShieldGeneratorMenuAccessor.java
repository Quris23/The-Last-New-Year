package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import com.anton.shieldgenerators.ShieldGeneratorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShieldGeneratorMenu.class, remap = false)
public interface ShieldGeneratorMenuAccessor {
    @Accessor("blockEntity")
    ShieldGeneratorBlockEntity sphereshields$getBlockEntity();
}
