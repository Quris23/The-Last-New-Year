package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Imbuing at the Arcane Anvil is allowed for: swords from any mod (the game's own default rule,
 * left alone), and Iron's Spellbooks' own non-armor items (wands, staves, ...). Everything else —
 * vanilla/other-mod tools, and armor from any mod including the spellbooks mod itself — is never
 * eligible, regardless of the mod's built-in spell-container/tag rules.
 */
@Mixin(targets = "io.redspace.ironsspellbooks.api.util.Utils", remap = false)
public class ImbueRestrictionMixin {
    @Inject(method = "canImbue", at = @At("HEAD"), cancellable = true)
    private static void spellclasses$restrictImbue(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        var item = itemStack.getItem();
        if (item instanceof ArmorItem) {
            cir.setReturnValue(false);
            return;
        }
        if (item instanceof SwordItem) {
            return; // let the mod's own logic grant it, as it already does for any sword
        }
        var id = BuiltInRegistries.ITEM.getKey(item);
        if (!id.getNamespace().equals("irons_spellbooks")) {
            cir.setReturnValue(false);
        }
    }
}
