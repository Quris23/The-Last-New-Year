package dev.qur1s.atlas.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/** Something shown on the Atlas that is not stored in it: another player, a Rift Gate or a Create train. */
public record AtlasLiveEntry(int kind, UUID id, int x, int y, int z, String name) {
    public static final int PLAYER = 0;
    public static final int GATE = 1;
    public static final int TRAIN = 2;
    public static final int STATION = 3;

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasLiveEntry> STREAM_CODEC = StreamCodec.of(
            (buf, e) -> {
                buf.writeByte(e.kind());
                buf.writeUUID(e.id());
                buf.writeInt(e.x());
                buf.writeInt(e.y());
                buf.writeInt(e.z());
                buf.writeUtf(e.name(), 160);
            },
            buf -> new AtlasLiveEntry(buf.readByte(), buf.readUUID(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(160)));
}
