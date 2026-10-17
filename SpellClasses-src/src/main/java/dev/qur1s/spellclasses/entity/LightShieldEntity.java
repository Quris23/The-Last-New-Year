package dev.qur1s.spellclasses.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * The dome of the "Light Shield" spell: a light-yellow sphere that follows its caster for {@link #DURATION_TICKS}.
 * Projectiles coming in from outside are stopped on its surface, hostile mobs are pushed out of it; whatever is
 * inside (and anything shot from inside) is left alone.
 */
public class LightShieldEntity extends Entity {
    public static final float RADIUS = 3.0f;
    public static final int DURATION_TICKS = 10 * 20;

    private static final DustParticleOptions SPARK = new DustParticleOptions(new Vector3f(1.0f, 0.93f, 0.45f), 1.2f);

    private UUID ownerId;

    public LightShieldEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerId = owner.getUUID();
    }

    public UUID ownerId() {
        return ownerId;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;

        LivingEntity owner = ownerId == null ? null : server.getEntity(ownerId) instanceof LivingEntity living ? living : null;
        if (owner == null || !owner.isAlive() || tickCount >= DURATION_TICKS) {
            discard();
            return;
        }
        setPos(owner.getX(), owner.getY() + owner.getBbHeight() * 0.5, owner.getZ());

        Vec3 center = position();
        var area = getBoundingBox().inflate(3.0);
        for (Projectile projectile : server.getEntitiesOfClass(Projectile.class, area)) {
            blockProjectile(server, projectile, center);
        }
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == owner || !(target instanceof Enemy) || target.getType().is(Tags.EntityTypes.BOSSES)) continue;
            Vec3 out = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(center);
            double dist = out.length();
            if (dist < RADIUS + 0.4) {
                Vec3 push = (dist < 0.01 ? new Vec3(1, 0, 0) : out.scale(1.0 / dist)).scale(0.45);
                target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.05, push.z));
                target.hurtMarked = true;
            }
        }
    }

    private void blockProjectile(ServerLevel server, Projectile projectile, Vec3 center) {
        if (projectile.getOwner() != null && projectile.getOwner().getUUID().equals(ownerId)) return;
        Vec3 now = projectile.position();
        Vec3 before = now.subtract(projectile.getDeltaMovement());
        double distNow = now.distanceTo(center);
        double distBefore = before.distanceTo(center);
        // only things moving in from outside: they were outside the sphere a tick ago and are on/inside it now
        if (distBefore <= RADIUS || distNow > RADIUS + 0.2) return;

        Vec3 hit = center.add(now.subtract(center).normalize().scale(RADIUS));
        server.sendParticles(SPARK, hit.x, hit.y, hit.z, 10, 0.25, 0.25, 0.25, 0.02);
        server.playSound(null, hit.x, hit.y, hit.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);
        projectile.discard();
    }
}
