package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dragonskin Spell Book ("Драконий Кодекс") defaults to 9 spell slots instead of the mod's usual 12. */
@Mixin(value = SpellBook.class, remap = false)
public class DragonskinSlotMixin {
    private static final int DRAGONSKIN_BASE_SLOTS = 9;

    @Inject(method = "getMaxSpellSlots", at = @At("HEAD"), cancellable = true)
    private void spellclasses$dragonskinBaseSlots(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == ItemRegistry.DRAGONSKIN_SPELL_BOOK.get()) {
            cir.setReturnValue(DRAGONSKIN_BASE_SLOTS);
        }
    }
}
