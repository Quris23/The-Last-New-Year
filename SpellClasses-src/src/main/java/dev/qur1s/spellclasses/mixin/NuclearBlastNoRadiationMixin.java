package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Create Nuclear's explosion entity burns everything near it with radiation damage; the Fire Arrow's blast deals explosion damage instead (no radiation), to everyone. */
@Mixin(targets = "net.nuclearteam.createnuclear.content.explosion.NuclearExplosionEntity", remap = false)
public abstract class NuclearBlastNoRadiationMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require = 0)
    private boolean spellclasses$noRadiation(LivingEntity target, DamageSource source, float amount) {
        if (((Entity) (Object) this).getPersistentData().getBoolean(dev.qur1s.spellclasses.FireArrowBlast.CLEAN_TAG)) {
            // the blast hurts everyone near it, the caster included, but as an ordinary explosion, not as radiation
            return target.hurt(target.damageSources().explosion(null, null), amount);
        }
        return target.hurt(source, amount);
    }
}
