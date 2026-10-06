package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.CrossGateAccess;
import dev.qur1s.spellclasses.compat.NetherGate;
import dev.qur1s.spellclasses.compat.ShipDimensionTransfer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.gate.GateFailure;
import uk.co.iceconchy.aerowarptics.gate.GateTicket;
import uk.co.iceconchy.aerowarptics.gate.GateTraversal;
import uk.co.iceconchy.aerowarptics.gate.GateWatch;
import uk.co.iceconchy.aerowarptics.gate.RiftGate;
import uk.co.iceconchy.aerowarptics.gate.RiftGateBlockEntity;
import uk.co.iceconchy.aerowarptics.gate.RiftGateRegistry;
import uk.co.iceconchy.aerowarptics.gate.RiftGateShape;
import uk.co.iceconchy.aerowarptics.gate.RiftGateState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Rift Gates across dimensions.
 *
 * AeroWarptics keeps every gate in one global registry but refuses to dial a gate in another dimension and does all
 * its bookkeeping (far-end lookup, chunk tickets, crossing) in the dialling gate's own level. Here the lookups are
 * pointed at the partner's level, and a connection between two dimensions gets its own crossing code: players and
 * mobs change dimension, assembled ships go through {@link ShipDimensionTransfer}.
 * The "Ад" gate (see {@link NetherGate}) is built on first use and is passive: no Rift Essence, no rotation.
 */
@Mixin(value = RiftGateBlockEntity.class, remap = false)
public abstract class RiftGateBlockEntityCrossMixin implements CrossGateAccess {
    @Shadow
    private UUID gateId;
    @Shadow
    private RiftGateShape shape;
    @Shadow
    private UUID connected;
    @Shadow
    private RiftGateState state;
    @Shadow
    @Final
    private Map<UUID, Integer> settling;
    @Shadow
    @Final
    private GateWatch watch;

    @Shadow
    protected abstract boolean stepped(UUID id, Vec3 point, Set<UUID> seen);

    @Shadow
    protected abstract AABB worldCatchment(@Nullable Airship airship, double depth);

    @Shadow
    protected abstract void refuse(ServerLevel level, Airship vehicle, Vec3 origin, Vec3 localCentreBefore,
                                   @Nullable Airship nearAirship);

    @Shadow
    protected abstract RiftGate farGate();

    @Shadow
    public abstract Airship airship();

    @Shadow
    public abstract void hangUp(GateFailure reason);

    /** Gate on the other end of the call being processed - tells the redirects which level to look in. */
    @Unique
    private UUID spellclasses$partner;

    // ---------------------------------------------------------------- CrossGateAccess

    @Override
    public void spellclasses$setGateId(UUID id) {
        this.gateId = id;
        ((BlockEntity) (Object) this).setChanged();
    }

    @Override
    public Map<UUID, Integer> spellclasses$settling() {
        return this.settling;
    }

    @Override
    public boolean spellclasses$isNetherGate() {
        return NetherGate.ID.equals(this.gateId);
    }

    // ---------------------------------------------------------------- dialling

    @Inject(method = "dial", at = @At("HEAD"))
    private void spellclasses$beforeDial(ServerPlayer player, UUID target, CallbackInfoReturnable<GateFailure> cir) {
        this.spellclasses$partner = target;
        if (NetherGate.ID.equals(target) && ((BlockEntity) (Object) this).getLevel() instanceof ServerLevel level) {
            NetherGate.ensure(level.getServer(), ((BlockEntity) (Object) this).getBlockPos());
        }
    }

    @Redirect(method = "dial", at = @At(value = "INVOKE",
            target = "Luk/co/iceconchy/aerowarptics/gate/RiftGate;isInSameDimension(Lnet/minecraft/server/level/ServerLevel;)Z"))
    private boolean spellclasses$anyDimension(RiftGate far, ServerLevel level) {
        return true;
    }

    @Inject(method = "hangUp", at = @At("HEAD"))
    private void spellclasses$beforeHangUp(GateFailure reason, CallbackInfo ci) {
        this.spellclasses$partner = this.connected;
    }

    @Inject(method = "renewTicket", at = @At("HEAD"))
    private void spellclasses$beforeRenew(CallbackInfo ci) {
        this.spellclasses$partner = this.connected;
    }

    @Redirect(method = {"dial", "hangUp", "markArrived"}, at = @At(value = "INVOKE",
            target = "Luk/co/iceconchy/aerowarptics/gate/RiftGateBlockEntity;resolve(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Luk/co/iceconchy/aerowarptics/gate/RiftGateBlockEntity;"))
    private RiftGateBlockEntity spellclasses$resolveInPartnerLevel(ServerLevel level, BlockPos pos) {
        ServerLevel where = this.spellclasses$partnerLevel(level, pos);
        return where.getBlockEntity(pos) instanceof RiftGateBlockEntity gate ? gate : null;
    }

    @Redirect(method = {"dial", "renewTicket"}, at = @At(value = "INVOKE",
            target = "Luk/co/iceconchy/aerowarptics/gate/GateTicket;hold(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;D)V"))
    private void spellclasses$holdInPartnerLevel(ServerLevel level, Vec3 centre, double opening) {
        RiftGate partner = this.spellclasses$partnerGate(level);
        ServerLevel where = level;
        if (partner != null && partner.shape().centre().equals(centre)) {
            ServerLevel other = level.getServer().getLevel(partner.dimension());
            if (other != null) {
                where = other;
            }
        }
        GateTicket.hold(where, centre, opening);
    }

    @Unique
    private RiftGate spellclasses$partnerGate(ServerLevel level) {
        return this.spellclasses$partner == null ? null : RiftGateRegistry.get(level).byId(this.spellclasses$partner);
    }

    @Unique
    private ServerLevel spellclasses$partnerLevel(ServerLevel level, BlockPos pos) {
        RiftGate partner = this.spellclasses$partnerGate(level);
        if (partner != null && partner.controller().equals(pos)) {
            ServerLevel other = level.getServer().getLevel(partner.dimension());
            if (other != null) {
                return other;
            }
        }
        return level;
    }

    // ---------------------------------------------------------------- the Nether gate is passive

    @Inject(method = "dialCost", at = @At("HEAD"), cancellable = true)
    private void spellclasses$freeDial(CallbackInfoReturnable<Integer> cir) {
        if (this.spellclasses$isNetherGate()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "upkeepCost", at = @At("HEAD"), cancellable = true)
    private void spellclasses$freeUpkeep(CallbackInfoReturnable<Integer> cir) {
        if (this.spellclasses$isNetherGate()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "isSpinningFastEnough", at = @At("HEAD"), cancellable = true)
    private void spellclasses$noPowerNeeded(CallbackInfoReturnable<Boolean> cir) {
        if (this.spellclasses$isNetherGate()) {
            cir.setReturnValue(true);
        }
    }

    // ---------------------------------------------------------------- crossing between dimensions

    @Inject(method = "tickTraversal", at = @At("HEAD"), cancellable = true)
    private void spellclasses$crossTraversal(CallbackInfo ci) {
        this.spellclasses$partner = this.connected;
        if (this.shape == null || !(((BlockEntity) (Object) this).getLevel() instanceof ServerLevel level)) {
            return;
        }
        RiftGate far = this.farGate();
        if (far == null || far.dimension().equals(level.dimension())) {
            return; // same dimension (or no partner): AeroWarptics' own code
        }
        ci.cancel();
        ServerLevel farLevel = level.getServer().getLevel(far.dimension());
        if (farLevel == null || !farLevel.isLoaded(far.controller())) {
            return; // the other side is not loaded yet - the chunk ticket brings it in
        }
        Airship nearAirship = this.airship();
        Airship farAirship = Airship.containing(farLevel, far.controller());
        Set<UUID> seen = new HashSet<>();
        this.spellclasses$crossVehicles(level, farLevel, far, nearAirship, farAirship, seen);
        this.spellclasses$crossEntities(level, farLevel, far, nearAirship, farAirship, seen);
        this.watch.retain(seen);
    }

    @Unique
    private static Vec3 spellclasses$toLocal(Vec3 world, @Nullable Airship ship) {
        return ship == null ? world : ship.toShip(world);
    }

    @Unique
    private static Vec3 spellclasses$toWorld(Vec3 local, @Nullable Airship ship) {
        return ship == null ? local : ship.toWorld(local);
    }

    @Unique
    private static Vec3 spellclasses$dirToLocal(Vec3 world, @Nullable Airship ship) {
        if (ship == null) {
            return world;
        }
        Vector3d v = ship.toShipDirection(new Vector3d(world.x, world.y, world.z));
        return new Vec3(v.x, v.y, v.z);
    }

    @Unique
    private static Vec3 spellclasses$dirToWorld(Vec3 local, @Nullable Airship ship) {
        if (ship == null) {
            return local;
        }
        Vector3d v = ship.toWorldDirection(new Vector3d(local.x, local.y, local.z));
        return new Vec3(v.x, v.y, v.z);
    }

    @Unique
    private void spellclasses$crossEntities(ServerLevel level, ServerLevel farLevel, RiftGate far,
                                            @Nullable Airship nearAirship, @Nullable Airship farAirship, Set<UUID> seen) {
        RiftGateBlockEntity farBe = farLevel.getBlockEntity(far.controller()) instanceof RiftGateBlockEntity g ? g : null;
        for (Entity entity : level.getEntities((Entity) null, this.worldCatchment(nearAirship, 3.0), e -> !e.isRemoved())) {
            SubLevel tracking;
            if (entity.isPassenger() || (tracking = Sable.HELPER.getTrackingOrVehicleSubLevel(entity)) != null
                    && (nearAirship == null || tracking != nearAirship.subLevel())) {
                continue;
            }
            Vec3 localPoint = spellclasses$toLocal(entity.position(), nearAirship);
            boolean stepped = this.stepped(entity.getUUID(), localPoint, seen);
            if (this.settling.containsKey(entity.getUUID()) || !stepped) {
                continue;
            }
            AABB bounds = entity.getBoundingBox();
            if (!far.shape().admits(far.shape().extentAcross(bounds), bounds.getYsize())) {
                continue;
            }
            GateTraversal.Arrival arrival = GateTraversal.map(this.shape, far.shape(), localPoint,
                    spellclasses$dirToLocal(entity.getDeltaMovement(), nearAirship));
            Vec3 pos = spellclasses$toWorld(arrival.position(), farAirship);
            Vec3 motion = spellclasses$dirToWorld(arrival.motion(), farAirship);
            float yaw = entity.getYRot() + arrival.yawDelta();
            if (nearAirship != null || farAirship != null) {
                Vec3 facing = spellclasses$dirToLocal(GateTraversal.directionOfYaw(entity.getYRot()), nearAirship);
                yaw = GateTraversal.yawOfDirection(spellclasses$dirToWorld(
                        GateTraversal.rotateYaw(facing, arrival.yawDelta()), farAirship));
            }
            UUID id = entity.getUUID();
            if (entity instanceof ServerPlayer player) {
                player.teleportTo(farLevel, pos.x, pos.y, pos.z, yaw, player.getXRot());
                player.setDeltaMovement(motion);
                player.hurtMarked = true;
                player.fallDistance = 0.0f;
            } else {
                Entity moved = entity.changeDimension(new DimensionTransition(farLevel, pos, motion, yaw,
                        entity.getXRot(), DimensionTransition.DO_NOTHING));
                if (moved != null) {
                    moved.fallDistance = 0.0f;
                }
            }
            this.settling.put(id, 20);
            this.watch.forget(id);
            if (farBe != null) {
                ((CrossGateAccess) (Object) farBe).spellclasses$settling().put(id, 20);
            }
        }
    }

    @Unique
    private void spellclasses$crossVehicles(ServerLevel level, ServerLevel farLevel, RiftGate far,
                                            @Nullable Airship nearAirship, @Nullable Airship farAirship, Set<UUID> seen) {
        AABB catchment = this.worldCatchment(nearAirship, 3.0);
        List<ServerSubLevel> crossing = new ArrayList<>();
        for (SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, (BoundingBox3dc) new BoundingBox3d(catchment))) {
            if (subLevel instanceof ServerSubLevel server && !server.isRemoved()
                    && (nearAirship == null || server != nearAirship.subLevel())) {
                crossing.add(server);
            }
        }
        RiftGateBlockEntity farBe = farLevel.getBlockEntity(far.controller()) instanceof RiftGateBlockEntity g ? g : null;
        for (ServerSubLevel sub : crossing) {
            Airship vehicle = Airship.of(sub);
            if (vehicle == null) {
                continue;
            }
            Vector3d p = vehicle.position() instanceof Vector3d v ? v : new Vector3d(vehicle.position());
            Vec3 origin = new Vec3(p.x, p.y, p.z);
            Vector3d c = vehicle.centre(new Vector3d());
            Vec3 centre = new Vec3(c.x, c.y, c.z);
            Vec3 localCentre = spellclasses$toLocal(centre, nearAirship);
            boolean stepped = this.stepped(vehicle.uuid(), localCentre, seen);
            if (this.settling.containsKey(vehicle.uuid()) || !stepped) {
                continue;
            }
            AABB bounds = vehicle.worldBounds().toMojang();
            if (!this.shape.admits(this.shape.extentAcross(bounds), bounds.getYsize())
                    || !far.shape().admits(far.shape().extentAcross(bounds), bounds.getYsize())) {
                this.refuse(level, vehicle, origin, localCentre, nearAirship);
                continue;
            }
            Vector3d vel = vehicle.velocity();
            GateTraversal.Arrival arrival = GateTraversal.map(this.shape, far.shape(), localCentre,
                    spellclasses$dirToLocal(new Vec3(vel.x, vel.y, vel.z), nearAirship));
            Vec3 worldArrival = spellclasses$toWorld(arrival.position(), farAirship);
            Vec3 fromCentre = origin.subtract(centre);
            Vec3 arrivalOrigin = worldArrival.add(GateTraversal.rotateYaw(fromCentre, arrival.yawDelta()));
            Quaterniond orientation = new Quaterniond(vehicle.orientation()).rotateY(Math.toRadians(-arrival.yawDelta()));
            this.settling.put(vehicle.uuid(), 40);
            this.watch.forget(vehicle.uuid());
            if (farBe != null) {
                ((CrossGateAccess) (Object) farBe).spellclasses$settling().put(vehicle.uuid(), 40);
            }
            ShipDimensionTransfer.transferLater(sub, level, farLevel,
                    new Vector3d(arrivalOrigin.x, arrivalOrigin.y, arrivalOrigin.z), orientation);
        }
    }

    // ---------------------------------------------------------------- ambient sound

    /** An open gate hums now and then, like a Nether portal does (the mod itself has no ambient sound for it). */
    @Inject(method = "tick", at = @At("RETURN"))
    private void spellclasses$ambientHum(CallbackInfo ci) {
        if (this.shape == null || this.state == null || !this.state.hasAperture()
                || !(((BlockEntity) (Object) this).getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.random.nextInt(this.state == RiftGateState.OPEN ? 70 : 140) != 0) {
            return;
        }
        Vec3 c = this.shape.centre();
        float volume = this.state == RiftGateState.OPEN ? 0.7f : 0.4f;
        level.playSound(null, c.x, c.y, c.z, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, volume,
                level.random.nextFloat() * 0.4f + 0.8f);
    }
}
