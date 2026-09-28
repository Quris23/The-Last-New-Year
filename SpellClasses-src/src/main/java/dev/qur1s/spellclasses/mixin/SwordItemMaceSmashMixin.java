package dev.qur1s.spellclasses.mixin;

import net.mcreator.borninchaosv.item.SkullCrusherItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Skullbreaker Hammer is a {@code SwordItem}, and {@code SwordItem} overrides
 * {@code postHurtEnemy} itself (just durability damage) - so {@link ItemMaceSmashMixin}'s
 * injection into the base {@code Item} method never runs for it. This adds the same
 * fall-distance reset here instead, guarded to only this one sword.
 */
@Mixin(value = SwordItem.class, remap = false)
public abstract class SwordItemMaceSmashMixin {
    @Inject(method = "postHurtEnemy", at = @At("HEAD"))
    private void spellclasses$smashResetFall(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (!((Object) this instanceof SkullCrusherItem)) return;
        if (MaceItem.canSmashAttack(attacker)) {
            attacker.resetFallDistance();
        }
    }
}
