package dev.qur1s.spellclasses.compat;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;

/**
 * Moves a whole Sable sub-level (an assembled ship) into another dimension.
 *
 * Sable ships are real blocks living in "plot" chunks of one {@link ServerLevel}, so the transfer is: allocate
 * an empty sub-level in the destination level, move every block (with its block-entity NBT) into the new plot
 * through Sable's own assembly helper, then re-spawn everything standing aboard and delete the old sub-level.
 * Unlike a plot NBT dump/restore this keeps block entities aligned even when the two dimensions have different
 * heights, because the helper works block by block through the live levels.
 */
public final class ShipDimensionTransfer {
    private static final Logger LOG = LoggerFactory.getLogger("spellclasses/shiptransfer");

    private ShipDimensionTransfer() {
    }

    /**
     * @param passengers entities to carry along (players are teleported, others are re-created); they are given
     *                   in world space of {@code origin}
     * @param target     where the ship's rotation point ends up in {@code dest}
     */
    public static boolean transfer(ServerSubLevel old, ServerLevel origin, ServerLevel dest, Vector3dc target,
                                   List<Entity> passengers) {
        return transfer(old, origin, dest, target, passengers, null);
    }

    /**
     * Same, but between ticks and with the crew worked out at that moment: everything riding the ship, plus
     * anyone standing on it. {@code orientation} replaces the ship's own when not null.
     */
    public static void transferLater(ServerSubLevel old, ServerLevel origin, ServerLevel dest, Vector3dc target,
                                     @Nullable Quaterniondc orientation) {
        origin.getServer().execute(() -> {
            if (old.isRemoved()) {
                return;
            }
            List<Entity> aboard = new ArrayList<>();
            dev.qur1s.spellclasses.compat.ShipDimensionTransfer.collectAboard(old, aboard);
            transfer(old, origin, dest, target, aboard, orientation);
        });
    }

    static void collectAboard(ServerSubLevel old, List<Entity> aboard) {
        uk.co.iceconchy.aerowarptics.airship.Airship ship = uk.co.iceconchy.aerowarptics.airship.Airship.of(old);
        if (ship == null) {
            return;
        }
        aboard.addAll(ship.passengers());
        for (ServerPlayer p : ship.crew()) {
            if (!aboard.contains(p)) {
                aboard.add(p);
            }
        }
    }

    public static boolean transfer(ServerSubLevel old, ServerLevel origin, ServerLevel dest, Vector3dc target,
                                   List<Entity> passengers, @Nullable Quaterniondc orientation) {
        try {
            return run(old, origin, dest, target, passengers, orientation);
        } catch (Throwable t) {
            LOG.error("Ship transfer {} -> {} failed", origin.dimension().location(), dest.dimension().location(), t);
            return false;
        }
    }

    private static boolean run(ServerSubLevel old, ServerLevel origin, ServerLevel dest, Vector3dc target,
                               List<Entity> passengers, @Nullable Quaterniondc orientation) {
        if (old.isRemoved()) {
            return false;
        }
        ServerSubLevelContainer oldContainer = SubLevelContainer.getContainer(origin);
        ServerSubLevelContainer newContainer = SubLevelContainer.getContainer(dest);
        if (oldContainer == null || newContainer == null) {
            LOG.error("Missing sub-level container (origin={}, dest={})", oldContainer, newContainer);
            return false;
        }
        LevelPlot oldPlot = old.getPlot();

        // 1. every block of the ship, in plot coordinates of the origin level
        List<BlockPos> blocks = new ArrayList<>();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (PlotChunkHolder holder : new ArrayList<>(oldPlot.getLoadedChunks())) {
            LevelChunk chunk = holder.getChunk();
            if (chunk == null) {
                continue;
            }
            ChunkPos cp = chunk.getPos();
            for (int s = 0; s < chunk.getSections().length; s++) {
                LevelChunkSection section = chunk.getSection(s);
                if (section.hasOnlyAir()) {
                    continue;
                }
                int baseY = (chunk.getMinSection() + s) << 4;
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            if (!section.getBlockState(x, y, z).isAir()) {
                                BlockPos p = new BlockPos(cp.getMinBlockX() + x, baseY + y, cp.getMinBlockZ() + z);
                                blocks.add(p);
                                minY = Math.min(minY, p.getY());
                                maxY = Math.max(maxY, p.getY());
                            }
                        }
                    }
                }
            }
        }
        if (blocks.isEmpty()) {
            LOG.error("Ship has no blocks to transfer");
            return false;
        }

        // 2. destination: preload the landing chunks, then allocate the new sub-level
        int cx = Math.floorDiv((int) Math.floor(target.x()), 16);
        int cz = Math.floorDiv((int) Math.floor(target.z()), 16);
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
                dest.getChunk(x, z, ChunkStatus.FULL, true);
            }
        }

        Vector3d oldRotationPoint = new Vector3d(old.logicalPose().rotationPoint());
        Pose3d spawn = new Pose3d();
        spawn.position().set(target);
        spawn.orientation().set(orientation != null ? orientation : old.logicalPose().orientation());
        ServerSubLevel fresh = (ServerSubLevel) newContainer.allocateNewSubLevel(spawn);
        LevelPlot newPlot = fresh.getPlot();
        newPlot.newEmptyChunk(newPlot.getCenterChunk());

        BlockPos oldCenter = oldPlot.getCenterBlock();
        BlockPos newCenter = newPlot.getCenterBlock();
        int dx = newCenter.getX() - oldCenter.getX();
        int dy = newCenter.getY() - oldCenter.getY();
        int dz = newCenter.getZ() - oldCenter.getZ();
        if (minY + dy < dest.getMinBuildHeight() || maxY + dy >= dest.getMaxBuildHeight()) {
            LOG.error("Ship ({}..{}) does not fit the height range of {}", minY, maxY, dest.dimension().location());
            newContainer.removeSubLevel(fresh, SubLevelRemovalReason.REMOVED);
            return false;
        }

        // Sable only creates missing plot chunks for the level it moves FROM, so create the new ones here.
        for (BlockPos p : blocks) {
            ChunkPos np = new ChunkPos((p.getX() + dx) >> 4, (p.getZ() + dz) >> 4);
            if (newPlot.getChunkHolder(newPlot.toLocal(np)) == null) {
                newPlot.newEmptyChunk(np);
            }
        }

        // Who sits where: remembered now, because the seats are removed together with their blocks.
        Map<Entity, BlockPos> seated = new IdentityHashMap<>();
        for (Entity e : passengers) {
            if (e.getVehicle() instanceof SeatEntity seat) {
                BlockPos at = seat.blockPosition();
                if (!(origin.getBlockState(at).getBlock() instanceof SeatBlock)
                        && origin.getBlockState(at.below()).getBlock() instanceof SeatBlock) {
                    at = at.below();
                }
                if (origin.getBlockState(at).getBlock() instanceof SeatBlock) {
                    seated.put(e, at.immutable());
                }
            }
        }

        // 3. blocks + block entities
        var transform = new SubLevelAssemblyHelper.AssemblyTransform(oldCenter, newCenter, 0, Rotation.NONE, dest);
        try {
            SubLevelAssemblyHelper.moveBlocks(origin, transform, blocks);
        } catch (UnsupportedOperationException e) {
            // Sable ends moveBlocks with a client-update call at the OLD plot positions in the destination
            // level, which has no plot there. Every block has been moved by then - it is only a notification.
            LOG.debug("Ignoring Sable's trailing cross-level update: {}", e.getMessage());
        }

        // 4. pose + physics
        Pose3d pose = fresh.logicalPose();
        pose.position().set(target);
        pose.orientation().set(orientation != null ? orientation : old.logicalPose().orientation());
        pose.rotationPoint().set(oldRotationPoint.x + dx, oldRotationPoint.y + dy, oldRotationPoint.z + dz);
        PhysicsPipeline pipeline = newContainer.physicsSystem().getPipeline();
        pipeline.teleport(fresh, pose.position(), pose.orientation());
        pipeline.resetVelocity(fresh);
        fresh.updateLastPose();
        fresh.updateBoundingBox();
        CompoundTag userData = old.getUserDataTag();
        if (userData != null) {
            fresh.setUserDataTag(userData.copy());
        }
        if (old.getName() != null) {
            fresh.setName(old.getName());
        }

        // 5. things that live inside the plot itself (item frames, paintings, ...) - seats are re-made on use
        AABB plotBox = new AABB(oldCenter.getX() - 400, origin.getMinBuildHeight(), oldCenter.getZ() - 400,
                oldCenter.getX() + 400, origin.getMaxBuildHeight(), oldCenter.getZ() + 400);
        for (Entity e : origin.getEntities((Entity) null, plotBox, x -> !(x instanceof Player) && !x.isRemoved()
                && oldPlot.contains(x.position()))) {
            if (e instanceof SeatEntity) {
                e.discard();
                continue;
            }
            recreate(e, dest, e.position().add(dx, dy, dz));
        }

        // 6. everyone aboard keeps their place relative to the deck
        Pose3d oldPose = new Pose3d(old.logicalPose());
        for (Entity e : passengers) {
            if (e.isRemoved()) {
                continue;
            }
            Vector3d local = oldPose.transformPositionInverse(new Vector3d(e.getX(), e.getY(), e.getZ()), new Vector3d());
            local.add(dx, dy, dz);
            Vector3d world = fresh.logicalPose().transformPosition(local, new Vector3d());
            BlockPos seatAt = seated.get(e);
            BlockPos newSeat = seatAt == null ? null : seatAt.offset(dx, dy, dz);
            Entity placed = e;
            if (e instanceof ServerPlayer player) {
                player.stopRiding();
                player.teleportTo(dest, world.x, world.y + 0.05, world.z, player.getYRot(), player.getXRot());
            } else {
                e.stopRiding();
                placed = recreate(e, dest, new Vec3(world.x, world.y, world.z));
            }
            if (newSeat != null && placed != null) {
                // Sit down a moment later: a rider's coordinates are the seat's (plot space, ~20,000,000 blocks
                // out), and the client must know about the new sub-level first or it shows the player out there.
                final Entity rider = placed;
                final BlockPos seatPos = newSeat;
                net.minecraft.server.MinecraftServer srv = dest.getServer();
                srv.tell(new net.minecraft.server.TickTask(srv.getTickCount() + 20, () -> {
                    if (!rider.isRemoved() && rider.level() == dest && !fresh.isRemoved()
                            && dest.getBlockState(seatPos).getBlock() instanceof SeatBlock) {
                        SeatBlock.sitDown(dest, seatPos, rider);
                    }
                }));
            }
        }

        // 7. the old ship should be empty now; if some block refused to move, keep it instead of deleting it
        int leftover = 0;
        for (BlockPos p : blocks) {
            if (!origin.getBlockState(p).isAir()) {
                leftover++;
            }
        }
        if (leftover > 0) {
            LOG.error("{} block(s) did not move - the old ship is kept in {} at {}", leftover,
                    origin.dimension().location(), old.logicalPose().position());
            for (Entity e : passengers) {
                if (e instanceof ServerPlayer player) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "Перенос корабля неполный: " + leftover + " блок(ов) остались на старом корабле"));
                }
            }
        } else {
            oldContainer.removeSubLevel(old, SubLevelRemovalReason.REMOVED);
        }
        // No inertia is carried over: the ship arrives at rest and is held at rest for a few ticks while the
        // new physics body settles.
        pipeline.resetVelocity(fresh);
        net.minecraft.server.MinecraftServer rest = dest.getServer();
        for (int delay : new int[]{1, 2, 5, 10}) {
            rest.tell(new net.minecraft.server.TickTask(rest.getTickCount() + delay, () -> {
                if (!fresh.isRemoved()) {
                    newContainer.physicsSystem().getPipeline().resetVelocity(fresh);
                }
            }));
        }
        LOG.info("Moved ship {} -> {} ({} blocks), new sub-level {}; requested position {}, pose now {}",
                origin.dimension().location(), dest.dimension().location(), blocks.size(), fresh.getUniqueId(),
                fmt(target), fmt(fresh.logicalPose().position()));
        net.minecraft.server.MinecraftServer server = dest.getServer();
        server.tell(new net.minecraft.server.TickTask(server.getTickCount() + 40, () -> LOG.info(
                "Ship {} two seconds after the transfer: pose {}, removed={}", fresh.getUniqueId(),
                fmt(fresh.logicalPose().position()), fresh.isRemoved())));
        return true;
    }

    private static String fmt(Vector3dc v) {
        return String.format("(%.1f, %.1f, %.1f)", v.x(), v.y(), v.z());
    }

    private static Entity recreate(Entity original, ServerLevel dest, Vec3 pos) {
        CompoundTag state = new CompoundTag();
        if (!original.saveAsPassenger(state) && !original.save(state)) {
            return null;
        }
        Entity copy = original.getType().create(dest);
        if (copy == null) {
            return null;
        }
        copy.load(state);
        original.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        copy.moveTo(pos.x, pos.y, pos.z, copy.getYRot(), copy.getXRot());
        dest.addFreshEntity(copy);
        return copy;
    }
}
