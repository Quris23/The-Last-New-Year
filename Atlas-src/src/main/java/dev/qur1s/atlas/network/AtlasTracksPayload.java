package dev.qur1s.atlas.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** S2C: track pieces as x1, z1, x2, z2 block coordinates, four ints each. */
public record AtlasTracksPayload(UUID atlasId, String dimension, int[] segments) implements CustomPacketPayload {
    public static final Type<AtlasTracksPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "tracks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasTracksPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUUID(p.atlasId());
                buf.writeUtf(p.dimension(), 128);
                buf.writeVarInt(p.segments().length);
                for (int v : p.segments()) buf.writeInt(v);
            },
            buf -> {
                UUID id = buf.readUUID();
                String dimension = buf.readUtf(128);
                int n = Math.min(buf.readVarInt(), AtlasMaxSegments.INTS);
                int[] segments = new int[n - n % 4];
                for (int i = 0; i < segments.length; i++) segments[i] = buf.readInt();
                return new AtlasTracksPayload(id, dimension, segments);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Upper bound on what a client accepts. */
    private static final class AtlasMaxSegments {
        static final int INTS = 20000 * 4;
    }
}
