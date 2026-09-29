package dev.qur1s.sphereshields;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Ground dome shields only block fast-moving objects (see shield_generators' own
 * {@code blockProjectileSpeed} config) - a hostile mob (or a player NOT on the generator's access
 * whitelist, see the shield GUI's access-control button) just walking in at normal speed goes
 * straight through otherwise. Every tick, pushes any such entity caught inside an active
 * {@link GroundShieldTracker} dome back toward the surface by directly overriding its velocity -
 * NOT by teleporting it to a computed boundary position. An early version teleported straight to the
 * geometric surface point, which (below the dome's center height, where the sphere is embedded in
 * unexcavated terrain) frequently landed the entity inside solid ground; Minecraft's own
 * anti-suffocation logic would then immediately shove it back out - toward the nearest open space,
 * which was usually further INSIDE the dome. That let mobs (skeletons especially) settle deeper each
 * time they got "pushed", eventually landing somewhere the base mod's fast-projectile reflection
 * doesn't catch, and firing from inside undisturbed. A pure velocity push never places the entity
 * anywhere - it just leans on it - so normal collision naturally stops it at whatever's actually
 * solid, with no bounce-back exploit.
 * <p>
 * The push direction still depends on which half of the sphere the entity is in: at/above the dome's
 * center height it's pushed straight out along the full 3D radial direction (needed so a near-vertical
 * fall through the pole, where the horizontal cross-section shrinks toward nothing, is still deflected
 * meaningfully); below center height it's pushed horizontally only, since a radial push there would
 * aim partly downward, straight into the ground.
 * <p>
 * Pushing stops as soon as the entity crosses {@code radius} exactly, which parks ranged attackers
 * (that keep trying to walk back to their preferred shooting distance) right on the boundary line
 * instead of clearly outside it - and a shot fired from a position still technically "inside" never
 * crosses the shield's own shell, so the base mod's projectile reflection never sees it and it hits
 * whatever's inside undisturbed. {@link #EJECT_BUFFER} keeps pushing for a bit past the radius so
 * ranged mobs actually end up outside the shell, not loitering right against it.
 */
@EventBusSubscriber(modid = "sphereshields", bus = EventBusSubscriber.Bus.GAME)
public final class HostileMobBarrier {
    private HostileMobBarrier() {
    }

    private static final double PUSH_SPEED = 0.5;
    private static final double EJECT_BUFFER = 0.75;

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        boolean isHostile = entity instanceof Enemy;
        boolean isPlayer = entity instanceof ServerPlayer;
        if (!isHostile && !isPlayer) return;
        if (isPlayer && (((ServerPlayer) entity).isCreative() || ((ServerPlayer) entity).isSpectator())) return;

        for (GroundShieldTracker.Dome dome : GroundShieldTracker.domesIn(serverLevel.dimension())) {
            if (isPlayer && dome.allowedPlayers().contains(((ServerPlayer) entity).getUUID())) continue;

            BlockPos center = dome.center();
            Vec3 centerVec = new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
            Vec3 offset = living.position().subtract(centerVec);
            double distance = offset.length();
            if (distance >= dome.radius() + EJECT_BUFFER) continue;

            double dy = offset.y;
            Vec3 pushDir;
            if (dy >= 0.0) {
                pushDir = distance < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : offset.scale(1.0 / distance);
                living.setDeltaMovement(pushDir.scale(PUSH_SPEED));
            } else {
                double horizDistance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
                double dirX = horizDistance < 1.0E-4 ? 1.0 : offset.x / horizDistance;
                double dirZ = horizDistance < 1.0E-4 ? 0.0 : offset.z / horizDistance;
                pushDir = new Vec3(dirX, 0.0, dirZ);
                Vec3 delta = living.getDeltaMovement();
                living.setDeltaMovement(dirX * PUSH_SPEED, delta.y, dirZ * PUSH_SPEED);
            }

            living.hurtMarked = true;
        }
    }
}
