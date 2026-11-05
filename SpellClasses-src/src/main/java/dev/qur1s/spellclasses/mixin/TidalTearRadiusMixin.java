package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Tidal Tear (Cataclysm: Spellbooks' Abyssal Slash) reaches five times as far: its 4.1 block radius is now 20.5. */
@Mixin(targets = "net.acetheeldritchking.cataclysm_spellbooks.spells.abyssal.AbyssalSlashSpell", remap = false)
public abstract class TidalTearRadiusMixin {
    @ModifyConstant(method = "onCast", constant = @Constant(floatValue = 4.1f), require = 0)
    private float spellclasses$fiveTimesRadius(float radius) {
        return radius * 5f;
    }
}
