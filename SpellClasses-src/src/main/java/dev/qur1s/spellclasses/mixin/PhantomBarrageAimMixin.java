package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Phantom Barrage (Somake's Spells): the arrows used to leave in parallel along the caster's look direction from
 * their ring behind the caster. Now each one flies towards the point the caster is looking at (the first block or
 * creature on the line of sight, or a point 64 blocks ahead), so they converge there.
 */
@Mixin(targets = "com.somake.somakespells.entity.PhantomBarrageProjectile", remap = false)
public abstract class PhantomBarrageAimMixin {
    private static final double RANGE = 64.0;

    @Redirect(method = "launch", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 spellclasses$towardsAimPoint(LivingEntity owner) {
        Vec3 look = owner.getLookAngle();
        Vec3 eye = owner.getEyePosition();
        Vec3 end = eye.add(look.scale(RANGE));
        var level = owner.level();
        HitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 aim = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        AABB box = owner.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(level, owner, eye, aim, box,
                e -> !e.isSpectator() && e.isPickable());
        if (entity != null) aim = entity.getLocation();
        Vec3 direction = aim.subtract(((Entity) (Object) this).position());
        return direction.lengthSqr() < 1.0E-4 ? look : direction.normalize();
    }
}
