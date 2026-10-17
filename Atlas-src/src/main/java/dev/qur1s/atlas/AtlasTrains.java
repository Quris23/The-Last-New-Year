package dev.qur1s.atlas;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasLiveEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Create's trains and tracks for the Atlas. Only touched when Create is loaded. */
public final class AtlasTrains {
    public static final int MAX_SEGMENTS = 20000;

    private AtlasTrains() {
    }

    /** One entry per train that has a carriage in the dimension: where its first carriage is, and its name. */
    public static void collectTrains(String dimension, List<AtlasLiveEntry> out) {
        for (Train train : Create.RAILWAYS.trains.values()) {
            if (train.carriages.isEmpty()) continue;
            Carriage first = train.carriages.get(0);
            for (ResourceKey<Level> dim : first.getPresentDimensions()) {
                if (!dim.location().toString().equals(dimension)) continue;
                first.getPositionInDimension(dim).ifPresent(pos -> out.add(new AtlasLiveEntry(AtlasLiveEntry.TRAIN, train.id,
                        pos.getX(), pos.getY(), pos.getZ(), train.name.getString())));
                break;
            }
        }
    }

    /** One entry per Create train station in the dimension. */
    public static void collectStations(String dimension, List<AtlasLiveEntry> out) {
        for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
            for (com.simibubi.create.content.trains.station.GlobalStation station
                    : graph.getPoints(com.simibubi.create.content.trains.graph.EdgePointType.STATION)) {
                if (!station.getBlockEntityDimension().location().toString().equals(dimension)) continue;
                BlockPos at = station.getBlockEntityPos();
                out.add(new AtlasLiveEntry(AtlasLiveEntry.STATION, station.id, at.getX(), at.getY(), at.getZ(), station.name));
            }
        }
    }

    /** Track pieces as x1, z1, x2, z2 in quarter blocks. */
    public static int[] collectTracks(AtlasData data, UUID atlasId, String dimension, boolean all) {
        List<int[]> segments = new ArrayList<>();
        for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
            if (isStub(graph)) continue;
            for (TrackNodeLocation location : graph.getNodes()) {
                if (!location.getDimension().location().toString().equals(dimension)) continue;
                TrackNode from = graph.locateNode(location);
                if (from == null) continue;
                for (Map.Entry<TrackNode, TrackEdge> entry : graph.getConnectionsFrom(from).entrySet()) {
                    TrackNode to = entry.getKey();
                    TrackEdge edge = entry.getValue();
                    if (edge.isInterDimensional()) continue;
                    // short dead ends (a track piece left sticking out sideways at a join) are not drawn
                    if (edge.getLength() < MAX_STUB_LENGTH && (graph.getConnectionsFrom(from).size() <= 1 || graph.getConnectionsFrom(to).size() <= 1)) continue;
                    // each piece once: from the lower id, or from here if the other side does not list it
                    if (from.getNetId() > to.getNetId() && graph.getConnectionsFrom(to).containsKey(from)) continue;
                    // curves are cut every couple of blocks, so that they look round
                    int steps = edge.getTurn() == null ? 1 : Math.max(2, Math.min(96, (int) Math.ceil(edge.getLength() / 2)));
                    // the position along the edge is a fraction (0 .. 1) of it
                    Vec3 previous = edge.getPosition(graph, 0.0);
                    for (int i = 1; i <= steps; i++) {
                        Vec3 next = edge.getPosition(graph, (double) i / steps);
                        int x1 = (int) Math.floor(previous.x), z1 = (int) Math.floor(previous.z);
                        int x2 = (int) Math.floor(next.x), z2 = (int) Math.floor(next.z);
                        if (all || data.isKnown(atlasId, dimension, x1, z1) || data.isKnown(atlasId, dimension, x2, z2)) {
                            segments.add(new int[]{(int) Math.round(previous.x * 4), (int) Math.round(previous.z * 4), (int) Math.round(next.x * 4), (int) Math.round(next.z * 4)});
                            if (segments.size() >= MAX_SEGMENTS) return flatten(segments);
                        }
                        previous = next;
                    }
                }
            }
        }
        return flatten(segments);
    }

    /** Graphs with fewer nodes than this are loose track pieces, left out unless a station or a train belongs to them. */
    private static final int MIN_NODES = 6;
    /** A dead-end edge shorter than this (blocks) is a stub, not a line. */
    private static final double MAX_STUB_LENGTH = 4.0;

    private static boolean isStub(TrackGraph graph) {
        if (graph.getNodes().size() >= MIN_NODES) return false;
        if (!graph.getPoints(com.simibubi.create.content.trains.graph.EdgePointType.STATION).isEmpty()) return false;
        for (Train train : Create.RAILWAYS.trains.values()) {
            if (train.graph == graph) return false;
        }
        return true;
    }

    private static int[] flatten(List<int[]> segments) {
        int[] out = new int[segments.size() * 4];
        for (int i = 0; i < segments.size(); i++) System.arraycopy(segments.get(i), 0, out, i * 4, 4);
        return out;
    }
}
