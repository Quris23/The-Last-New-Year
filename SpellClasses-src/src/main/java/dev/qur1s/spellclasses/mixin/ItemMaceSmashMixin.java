package dev.qur1s.spellclasses.mixin;

import com.github.L_Ender.cataclysm.items.Brontes;
import com.github.L_Ender.cataclysm.items.Infernal_forge;
import com.github.L_Ender.cataclysm.items.Void_forge;
import net.mcreator.borninchaosv.item.SkullCrusherItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The bonus-damage half of the Mace smash mechanic for Brontes, the Infernal Forge, the Void Forge
 * and the Skullbreaker Hammer - see {@link CataclysmMaceSmashMixin} for the knockback/sound half.
 * None of them override {@code getAttackDamageBonus}, so they fall through to this base
 * {@code Item} method, which is exactly what's injected here (guarded by an instanceof check,
 * same pattern as {@code AbstractSpellOverridesMixin}'s per-spell overrides). The no-fall-damage
 * reset ({@code postHurtEnemy}) is here too for the three Cataclysm items, but the Skullbreaker
 * Hammer needs its own ({@link SwordItemMaceSmashMixin}) since {@code SwordItem} overrides that
 * method itself.
 */
@Mixin(value = Item.class, remap = false)
public abstract class ItemMaceSmashMixin {
    private boolean spellclasses$isMaceSmashItem() {
        return (Object) this instanceof Brontes || (Object) this instanceof Infernal_forge
                || (Object) this instanceof Void_forge || (Object) this instanceof SkullCrusherItem;
    }

    @Inject(method = "getAttackDamageBonus", at = @At("HEAD"), cancellable = true)
    private void spellclasses$smashDamageBonus(Entity target, float damage, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {
        if (!spellclasses$isMaceSmashItem()) return;

        if (!(damageSource.getDirectEntity() instanceof LivingEntity attacker) || !MaceItem.canSmashAttack(attacker)) {
            cir.setReturnValue(0.0F);
            return;
        }

        float fallDistance = attacker.fallDistance;
        float bonus;
        if (fallDistance <= 3.0F) {
            bonus = 4.0F * fallDistance;
        } else if (fallDistance <= 8.0F) {
            bonus = 12.0F + 2.0F * (fallDistance - 3.0F);
        } else {
            bonus = 22.0F + fallDistance - 8.0F;
        }
        cir.setReturnValue(bonus);
    }

    @Inject(method = "postHurtEnemy", at = @At("HEAD"))
    private void spellclasses$smashResetFall(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (!spellclasses$isMaceSmashItem()) return;
        if (MaceItem.canSmashAttack(attacker)) {
            attacker.resetFallDistance();
        }
    }
}
