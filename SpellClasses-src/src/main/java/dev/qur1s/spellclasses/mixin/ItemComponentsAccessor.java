package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Item's default {@code components} field is {@code private final} with no vanilla setter - this
 * lets {@link dev.qur1s.spellclasses.StaffAttributeOverrides} replace another mod's already-built
 * item defaults (e.g. swap out an attribute_modifiers component) after registration, without
 * needing KubeJS's own (script-only) component-override machinery.
 */
@Mixin(Item.class)
public interface ItemComponentsAccessor {
    @Accessor("components")
    @Mutable
    void spellclasses$setComponents(DataComponentMap components);
}
