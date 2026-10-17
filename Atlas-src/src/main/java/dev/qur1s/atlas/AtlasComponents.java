package dev.qur1s.atlas;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

/** Data components of the Atlas item: its unique id, under which the map data lives on the server. */
public final class AtlasComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(AtlasMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> ATLAS_ID =
            COMPONENTS.registerComponentType("atlas_id", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
                    .cacheEncoding());

    /** How many chunks the Atlas covers (-1 when it is empty); kept up to date by the server while it is carried. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ATLAS_CHUNKS =
            COMPONENTS.registerComponentType("atlas_chunks", builder -> builder
                    .persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    private AtlasComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
