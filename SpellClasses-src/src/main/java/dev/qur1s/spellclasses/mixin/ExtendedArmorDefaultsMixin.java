package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.StaffAttributeOverrides;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Iron's Spellbooks armor pieces build their attributes lazily in {@code getDefaultAttributeModifiers()}, so setting
 * the item's component once at startup does not always stick. The Light-bearer's Chestplate (paladin_chestplate)
 * is changed from 8 to 16 armor at the source instead, whichever way the attributes are read.
 */
@Mixin(targets = "io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem", remap = false)
public abstract class ExtendedArmorDefaultsMixin {
    private static final ResourceLocation PALADIN_CHESTPLATE =
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "paladin_chestplate");

    @Inject(method = "getDefaultAttributeModifiers", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$paladinArmor(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (PALADIN_CHESTPLATE.equals(BuiltInRegistries.ITEM.getKey((Item) (Object) this))) {
            cir.setReturnValue(StaffAttributeOverrides.withArmor(cir.getReturnValue(), 16.0,
                    EquipmentSlotGroup.CHEST, ResourceLocation.fromNamespaceAndPath("spellclasses", "paladin_chestplate_armor")));
        }
    }
}
