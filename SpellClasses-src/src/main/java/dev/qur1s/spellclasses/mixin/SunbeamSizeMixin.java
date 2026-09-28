package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Doubles Sunbeam's strike radius and doubles its windup before it hits (15 -> 30 ticks). */
@Mixin(value = SunbeamEntity.class, remap = false)
public abstract class SunbeamSizeMixin extends AoeEntity {
    private static final float RADIUS_MULTIPLIER = 2.0f;
    private static final int NEW_WARMUP_TICKS = 30;

    private SunbeamSizeMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void spellclasses$increaseRadius(EntityType<? extends Projectile> entityType, Level level, CallbackInfo ci) {
        this.setRadius(this.getRadius() * RADIUS_MULTIPLIER);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 15, ordinal = 0))
    private int spellclasses$delayHitCheck(int original) {
        return NEW_WARMUP_TICKS;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 15, ordinal = 1))
    private int spellclasses$delayDiscard(int original) {
        return NEW_WARMUP_TICKS;
    }
}
