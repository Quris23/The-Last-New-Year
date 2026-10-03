package dev.qur1s.spellclasses.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchorAccess;
import uk.co.iceconchy.aerowarptics.gate.RiftGate;
import uk.co.iceconchy.aerowarptics.gate.RiftGateBlockEntity;
import uk.co.iceconchy.aerowarptics.gate.RiftGateRegistry;
import uk.co.iceconchy.aerowarptics.gate.RiftGateShape;
import uk.co.iceconchy.aerowarptics.registry.AWBlocks;

import java.util.UUID;

/**
 * The Rift Gate's built-in way down: a permanent "Ад" destination in every gate's list. The first time anybody
 * dials it, a Rift Gate is built in the Nether (at the dialling gate's coordinates divided by 8, like the old
 * vanilla portal) and from then on it is an ordinary entry in the registry. The gate down there is "passive":
 * dialling out of it costs no Rift Essence and needs no rotation, so nobody is stranded below.
 */
public final class NetherGate {
    public static final UUID ID = UUID.nameUUIDFromBytes("spellclasses:nether_gate".getBytes());
    public static final String NAME = "Ад";
    private static final Logger LOG = LoggerFactory.getLogger("spellclasses/nethergate");

    private static final int OPENING_W = 3;
    private static final int OPENING_H = 4;

    private NetherGate() {
    }

    /** The list entry shown until the real gate exists. */
    public static RiftGate placeholder() {
        RiftGateShape shape = new RiftGateShape(Direction.Axis.X, 0, 64, 0, OPENING_W - 1, 64 + OPENING_H - 1, 0);
        return new RiftGate(ID, NAME, Level.NETHER, new BlockPos(0, 64, 0), shape, null, "", WarpAnchorAccess.PUBLIC, "", true);
    }

    public static boolean exists(MinecraftServer server) {
        return RiftGateRegistry.get(server).byId(ID) != null;
    }

    /** Builds the Nether gate if it is not there yet. {@code from} is the overworld gate that is dialling. */
    public static boolean ensure(MinecraftServer server, BlockPos from) {
        if (exists(server)) {
            return true;
        }
        ServerLevel nether = server.getLevel(Level.NETHER);
        if (nether == null) {
            return false;
        }
        try {
            return build(nether, from.getX() / 8, from.getZ() / 8);
        } catch (Throwable t) {
            LOG.error("Could not build the Nether gate", t);
            return false;
        }
    }

    private static boolean build(ServerLevel nether, int x, int z) {
        int half = 6;
        for (int cx = (x - half) >> 4; cx <= (x + half) >> 4; cx++) {
            for (int cz = (z - half) >> 4; cz <= (z + half) >> 4; cz++) {
                nether.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        int y = floorLevel(nether, x, z);

        // clear a room and lay a floor
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState floor = Blocks.OBSIDIAN.defaultBlockState();
        for (int dx = -3; dx <= OPENING_W + 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                nether.setBlock(new BlockPos(x + dx, y - 2, z + dz), floor, 3);
                for (int dy = -1; dy <= OPENING_H + 3; dy++) {
                    nether.setBlock(new BlockPos(x + dx, y + dy, z + dz), air, 2);
                }
            }
        }

        // the controller first (the ring is still open, so it does not form yet) so it can take its fixed id
        BlockPos controller = new BlockPos(x + OPENING_W / 2, y - 1, z);
        nether.setBlock(controller, AWBlocks.RIFT_GATE.get().defaultBlockState(), 3);
        BlockEntity be = nether.getBlockEntity(controller);
        if (!(be instanceof RiftGateBlockEntity gate)) {
            LOG.error("Rift Gate block entity missing at {}", controller);
            return false;
        }
        ((CrossGateAccess) gate).spellclasses$setGateId(ID);

        BlockState frame = AWBlocks.RIFT_GATE_FRAME.get().defaultBlockState();
        for (int i = -1; i <= OPENING_W; i++) {
            for (int j = -1; j <= OPENING_H; j++) {
                boolean edge = i == -1 || i == OPENING_W || j == -1 || j == OPENING_H;
                BlockPos p = new BlockPos(x + i, y + j, z);
                if (edge && !p.equals(controller)) {
                    nether.setBlock(p, frame, 3);
                }
            }
        }
        boolean formed = gate.tryForm();
        RiftGateRegistry.get(nether).rename(ID, NAME);
        LOG.info("Built the Nether gate at {} (formed: {})", controller, formed);
        return formed;
    }

    private static int floorLevel(ServerLevel nether, int x, int z) {
        for (int y = Math.min(110, nether.getMaxBuildHeight() - 30); y > 35; y--) {
            BlockPos below = new BlockPos(x, y - 1, z);
            if (nether.getBlockState(below).isSolidRender(nether, below)
                    && nether.getBlockState(below).getFluidState().isEmpty()
                    && nether.getBlockState(new BlockPos(x, y, z)).isAir()
                    && nether.getBlockState(new BlockPos(x, y + 1, z)).isAir()
                    && nether.getBlockState(new BlockPos(x, y + 2, z)).isAir()) {
                return y;
            }
        }
        return 64;
    }
}
