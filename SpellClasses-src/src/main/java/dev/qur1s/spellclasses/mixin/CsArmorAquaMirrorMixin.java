package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.StaffAttributeOverrides;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cataclysm: Spellbooks armor builds its attributes lazily in {@code getDefaultAttributeModifiers()}, so the Abyssal
 * Warlock set is given its Aqua twin of every Abyssal buff here, whenever the attributes are read.
 */
@Mixin(targets = "net.acetheeldritchking.cataclysm_spellbooks.items.armor.CSArmorItem", remap = false)
public abstract class CsArmorAquaMirrorMixin {
    @Inject(method = "getDefaultAttributeModifiers", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$aquaMirror(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        Item self = (Item) (Object) this;
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(self).getPath();
        if (path.startsWith("abyssal_warlock_")) {
            cir.setReturnValue(StaffAttributeOverrides.withAquaMirror(self, cir.getReturnValue()));
        }
    }
}
