package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.spells.lightning.LightningBoltSpell;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lightning Bolt (Iron's Spellbooks) already charges creepers it hits (thunderHit, as vanilla lightning does) but
 * also damages them first; creepers now only get charged.
 */
@Mixin(value = LightningBoltSpell.class, remap = false)
public abstract class LightningBoltCreeperMixin {
    @Redirect(method = "lambda$onCast$1", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/damage/DamageSources;applyDamage(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z",
            remap = false), require = 0)
    private boolean spellclasses$noDamageToCreepers(Entity target, float amount, DamageSource source) {
        if (target instanceof Creeper) {
            return false;
        }
        return DamageSources.applyDamage(target, amount, source);
    }
}
