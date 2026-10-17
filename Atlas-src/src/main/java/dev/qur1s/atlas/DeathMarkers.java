package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.UUID;

/** When a player dies with an Atlas in the Atlas slot, a grave is put on that Atlas where they died. */
@EventBusSubscriber(modid = "atlas")
public final class DeathMarkers {
    /** How many graves an Atlas keeps per dimension; the oldest go first. */
    private static final int MAX_GRAVES = 5;

    private DeathMarkers() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) return;
        var atlas = Atlases.equippedStack(player);
        if (atlas.isEmpty()) return;
        UUID id = AtlasItem.ensureId(atlas);
        BlockPos pos = player.blockPosition();
        String dimension = player.level().dimension().location().toString();
        AtlasMarker grave = new AtlasMarker(UUID.randomUUID(), pos.getX(), pos.getY(), pos.getZ(),
                AtlasMarker.COLOR_DEATH, "", "grave", "");
        AtlasData data = AtlasData.get(player.getServer());
        data.addMarker(id, dimension, grave);
        data.trimMarkers(id, dimension, AtlasMarker.COLOR_DEATH, MAX_GRAVES);
    }
}
