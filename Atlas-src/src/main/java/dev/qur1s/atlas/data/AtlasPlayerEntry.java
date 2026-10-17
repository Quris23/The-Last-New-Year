package dev.qur1s.atlas.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/** A player who is online, for the player search list: where they are, in any dimension. */
public record AtlasPlayerEntry(UUID id, String name, String dimension, int x, int z) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasPlayerEntry> STREAM_CODEC = StreamCodec.of(
            (buf, e) -> {
                buf.writeUUID(e.id());
                buf.writeUtf(e.name(), 64);
                buf.writeUtf(e.dimension(), 128);
                buf.writeInt(e.x());
                buf.writeInt(e.z());
            },
            buf -> new AtlasPlayerEntry(buf.readUUID(), buf.readUtf(64), buf.readUtf(128), buf.readInt(), buf.readInt()));
}
