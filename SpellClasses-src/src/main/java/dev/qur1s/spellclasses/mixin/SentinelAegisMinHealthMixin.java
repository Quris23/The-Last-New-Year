package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.AegisMath;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sentinel Aegis (Ancient Remnants) adds {@code 20 * easeInCircle(1 - health / maxHealth)} armor. Its curve only
 * reaches the full +20 at exactly 0 health, and it depends on the player's own maximum, so a player whose maximum
 * was shrunk to a few points gets nothing at full health. Reworked here:
 * <ul>
 *   <li>the scale is measured against at least the vanilla maximum ({@link AegisMath#REFERENCE_MAX} = 20 health, ten hearts),
 *       not the player's own;</li>
 *   <li>the full +20 is reached at {@link AegisMath#FULL_BONUS_AT} = 2 health (one heart) and below.</li>
 * </ul>
 * With {@code R = max(maxHealth, 20)} the fraction becomes {@code (R - health) / (R - 2)}. The mod computes
 * {@code 1 - health / maxHealth}, so feeding it {@code health - 2} and {@code R - 2} gives exactly that (the value is
 * clamped to 0..1 by the mod itself, so anything at 2 health or less counts as 1).
 */
@Mixin(targets = "dev.obscuria.ancient_remnants.common.effect.SentinelAegisEffect", remap = false)
public abstract class SentinelAegisMinHealthMixin {
    @Redirect(method = "modifyArmorValue", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getHealth()F"))
    private float spellclasses$shiftedHealth(LivingEntity self) {
        return self.getHealth() - AegisMath.FULL_BONUS_AT;
    }

    @Redirect(method = "modifyArmorValue", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F"))
    private float spellclasses$shiftedMaxHealth(LivingEntity self) {
        return Math.max(self.getMaxHealth(), AegisMath.REFERENCE_MAX) - AegisMath.FULL_BONUS_AT;
    }
}
