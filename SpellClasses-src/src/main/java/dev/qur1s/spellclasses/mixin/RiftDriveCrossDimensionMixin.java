package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.CrossFix;
import dev.qur1s.spellclasses.compat.ShipDimensionTransfer;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.co.iceconchy.aerowarptics.warp.WarpCourse;
import uk.co.iceconchy.aerowarptics.warp.WarpFailure;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundSource;
import uk.co.iceconchy.aerowarptics.registry.AWSounds;
import java.util.ArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchor;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchorRegistry;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveBlockEntity;
import uk.co.iceconchy.aerowarptics.network.AWNetwork;
import uk.co.iceconchy.aerowarptics.network.ClientboundCorridorPacket;
import uk.co.iceconchy.aerowarptics.warp.WarpFlight;

import java.util.List;
import java.util.UUID;

/**
 * Replaces the in-dimension "across the fold" teleport with a real ship transfer when the flight's
 * destination anchor is in another dimension. The warp is completed on the old drive first (so the
 * block-entity NBT that gets copied says COOLDOWN, not WARPING - AeroWarptics turns a reloaded
 * mid-flight drive into an ERROR), then cnbridge moves the whole Sable plot, crew included.
 */
@Mixin(value = RiftDriveBlockEntity.class, remap = false)
public abstract class RiftDriveCrossDimensionMixin {
    @Unique
    private static final Logger SPELLCLASSES$LOG = LoggerFactory.getLogger("spellclasses/riftcross");

    /** Destination dimension of a beacon-summoned (fix) flight that crosses dimensions; null otherwise. */
    @Unique
    private ResourceKey<Level> spellclasses$crossDimension;

    @Inject(method = "startWarp", at = @At("HEAD"))
    private void spellclasses$rememberCrossDimension(WarpCourse course, ServerPlayer player,
                                                     CallbackInfoReturnable<WarpFailure> cir) {
        this.spellclasses$crossDimension = CrossFix.DIMENSION.get();
    }

    /** beginWarp: a fix course normally lands in the drive's own level - use the beacon user's level instead. */
    @ModifyVariable(method = "beginWarp", index = 3, at = @At(value = "STORE", ordinal = 1))
    private ServerLevel spellclasses$fixDestination(ServerLevel sameLevel) {
        if (this.spellclasses$crossDimension != null) {
            ServerLevel target = sameLevel.getServer().getLevel(this.spellclasses$crossDimension);
            if (target != null) {
                return target;
            }
        }
        return sameLevel;
    }

    @Shadow
    private WarpFlight flight;
    @Shadow
    private UUID destinationAnchor;

    @Shadow
    private void completeWarp() {
    }

    @Shadow
    private int effectiveColour() {
        return 0;
    }

    @Shadow
    private float effectiveIntensity() {
        return 1.0f;
    }

    @Redirect(method = "tickFlight",
            at = @At(value = "INVOKE", target = "Luk/co/iceconchy/aerowarptics/warp/WarpFlight;tick(Luk/co/iceconchy/aerowarptics/airship/Airship;)Luk/co/iceconchy/aerowarptics/warp/WarpFlight$Step;"))
    private WarpFlight.Step spellclasses$crossDimension(WarpFlight current, Airship airship) {
        WarpFlight.Step step = current.tick(airship);
        if (step != WarpFlight.Step.EXIT_CORRIDOR) {
            return step;
        }
        Level here = ((BlockEntity) (Object) this).getLevel();
        if (!(here instanceof ServerLevel origin)) {
            return step;
        }
        WarpAnchor anchor = this.destinationAnchor == null ? null
                : WarpAnchorRegistry.get(origin).byId(this.destinationAnchor);
        ServerLevel destination;
        String destinationName;
        if (anchor != null) {
            destination = origin.getServer().getLevel(anchor.dimension());
            destinationName = anchor.displayName() + " " + anchor.pos();
        } else if (this.spellclasses$crossDimension != null) {
            destination = origin.getServer().getLevel(this.spellclasses$crossDimension);
            destinationName = "beacon";
        } else {
            return step;
        }
        this.spellclasses$crossDimension = null;
        if (destination == null || destination.dimension().equals(origin.dimension())) {
            return step;
        }

        List<ServerPlayer> crew = airship.crew();
        Vector3d arrival = new Vector3d(current.arrivalOrigin());
        SPELLCLASSES$LOG.info("Cross-dimension jump to {} in {}: flight arrival origin is ({}, {}, {}), emergence origin ({}, {}, {})",
                destinationName, destination.dimension().location(),
                arrival.x, arrival.y, arrival.z,
                current.emergenceOrigin().x(), current.emergenceOrigin().y(), current.emergenceOrigin().z());
        ServerSubLevel subLevel = airship.subLevel();
        // Keep the tunnel on screen for the run out of the far rift, exactly like an in-dimension jump does after
        // the fold; it is re-sent once the crew is in the new dimension (a level change resets client overlays).
        final int tunnelTicks = Math.max(20, current.transitTicks());
        final int tunnelColour = this.effectiveColour();
        final float tunnelIntensity = this.effectiveIntensity();
        for (ServerPlayer player : crew) {
            AWNetwork.sendTo(player, ClientboundCorridorPacket.enter(tunnelTicks, tunnelColour, tunnelIntensity));
        }
        // The old drive finishes its warp first so the block-entity NBT that gets copied says COOLDOWN, not
        // WARPING (AeroWarptics turns a reloaded mid-flight drive into an ERROR).
        this.completeWarp();

        // Run between ticks, not from inside the drive's own block-entity tick.
        origin.getServer().execute(() -> {
            if (subLevel.isRemoved()) {
                return;
            }
            Airship ship = Airship.of(subLevel);
            List<Entity> aboard = new ArrayList<>();
            if (ship != null) {
                aboard.addAll(ship.passengers());
                for (ServerPlayer p : ship.crew()) {
                    if (!aboard.contains(p)) {
                        aboard.add(p);
                    }
                }
            }
            for (ServerPlayer p : crew) {
                if (!aboard.contains(p)) {
                    aboard.add(p);
                }
            }
            if (!ShipDimensionTransfer.transfer(subLevel, origin, destination, arrival, aboard)) {
                SPELLCLASSES$LOG.error("Ship transfer to {} failed - ship stays in {}",
                        destination.dimension().location(), origin.dimension().location());
                for (ServerPlayer p : crew) {
                    AWNetwork.sendTo(p, ClientboundCorridorPacket.leave());
                }
                return;
            }
            MinecraftServer server = destination.getServer();
            int now = server.getTickCount();
            server.tell(new TickTask(now + 5, () -> {
                for (ServerPlayer p : crew) {
                    if (p.serverLevel() == destination) {
                        AWNetwork.sendTo(p, ClientboundCorridorPacket.enter(tunnelTicks - 5, tunnelColour, tunnelIntensity));
                    }
                }
            }));
            server.tell(new TickTask(now + tunnelTicks, () -> {
                destination.playSound(null, arrival.x, arrival.y, arrival.z, AWSounds.WARP_EXIT.get(),
                        SoundSource.PLAYERS, 1.3f, 1.0f);
                for (ServerPlayer p : crew) {
                    AWNetwork.sendTo(p, ClientboundCorridorPacket.leave());
                }
            }));
        });
        return WarpFlight.Step.CONTINUE;
    }

    /**
     * A ship transfer (ours, or cnbridge's own Dimensional Drive) removes the old plot in the middle of the
     * tick, but the drive's block entity is still in this tick's ticking list and its trailing sendData()
     * then dies with "Cannot change blocks in nonexistent plot holder". Nothing is left to sync, so ignore it.
     */
    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Luk/co/iceconchy/aerowarptics/drive/RiftDriveBlockEntity;sendData()V"))
    private void spellclasses$safeSendData(RiftDriveBlockEntity drive) {
        try {
            drive.sendData();
        } catch (UnsupportedOperationException ignored) {
            // plot already removed by a dimension transfer
        }
    }
}
