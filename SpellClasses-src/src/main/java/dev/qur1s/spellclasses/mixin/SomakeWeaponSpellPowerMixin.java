package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.StaffAttributeOverrides;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Somake's weapons build their attributes lazily, so their spell power is rewritten here whenever they are read: Core
 * Splitter and Witherite Glaive get Fire, cast time reduction and spell power instead of Lightning; the Copper Glove
 * gives none while held (its Storm power comes from the hands curio slot, see CopperGloveTweak).
 */
@Mixin(targets = {"com.somake.somakespells.item.CoreSplitterItem", "com.somake.somakespells.item.WitheriteGlaiveItem",
        "com.somake.somakespells.item.BaseAttributeSwordItem", "com.somake.somakespells.item.TidePiercerItem"}, remap = false)
public abstract class SomakeWeaponSpellPowerMixin {
    @Inject(method = "getDefaultAttributeModifiers", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$swapSchool(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        cir.setReturnValue(StaffAttributeOverrides.somakeWeapon((Item) (Object) this, cir.getReturnValue()));
    }
}
