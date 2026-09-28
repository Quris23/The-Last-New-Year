package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * The Inscription Table's own screen hardcodes a 15-slot display cap (its background art only
 * has room for 5x3), one step below our raised {@link SpellSlotUpgradeCapMixin} cap of 16 - the
 * 16th slot existed on the book but had nowhere to render, so it silently couldn't be filled.
 * Bumping both literals lets it lay out (and auto-arrange, via the game's own row-balancing) all
 * 16 slots instead of only 15.
 */
@Mixin(value = InscriptionTableScreen.class, remap = false)
public abstract class InscriptionTableSlotCapMixin {
    @ModifyConstant(method = "generateSpellSlots", constant = {
            @Constant(intValue = 15, ordinal = 0),
            @Constant(intValue = 15, ordinal = 1)
    })
    private int spellclasses$raiseInscriptionTableCap(int original) {
        return 16;
    }
}
