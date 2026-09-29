package dev.qur1s.sphereshields.network;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.AllowedPlayersHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "sphereshields", bus = EventBusSubscriber.Bus.MOD)
public final class NetworkHandler {
    private NetworkHandler() {
    }

    private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0 * 64.0;

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SetAllowedPlayersPayload.TYPE, SetAllowedPlayersPayload.STREAM_CODEC, NetworkHandler::handleSetAllowedPlayers);
    }

    private static void handleSetAllowedPlayers(SetAllowedPlayersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockPos pos = payload.generatorPos();
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_INTERACTION_DISTANCE_SQR) return;

            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (!(blockEntity instanceof ShieldGeneratorBlockEntity)) return;
            if (!(blockEntity instanceof AllowedPlayersHolder holder)) return;

            Set<UUID> allowed = new LinkedHashSet<>(payload.allowed());
            holder.sphereshields$setAllowedPlayers(allowed);
            blockEntity.setChanged();
            if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }
        });
    }
}
