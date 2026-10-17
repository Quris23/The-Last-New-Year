package dev.qur1s.legspyglass;

import dev.qur1s.legspyglass.network.ScopeBroadcastPayload;
import dev.qur1s.legspyglass.network.ScopeStatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server side: who is currently looking through a spyglass, checked against the Curios slot and broadcast to nearby players. */
public final class Spyglasses {
    private Spyglasses() {
    }

    private static final Set<UUID> SCOPING = new HashSet<>();

    /** True if the entity has a spyglass in its Curios spyglass slot. */
    public static boolean hasSlotted(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(inventory -> inventory.findFirstCurio(Items.SPYGLASS).isPresent())
                .orElse(false);
    }

    private static void set(ServerPlayer player, boolean scoping) {
        boolean changed = scoping ? SCOPING.add(player.getUUID()) : SCOPING.remove(player.getUUID());
        if (changed) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    scoping ? net.minecraft.sounds.SoundEvents.SPYGLASS_USE : net.minecraft.sounds.SoundEvents.SPYGLASS_STOP_USING,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new ScopeBroadcastPayload(player.getId(), scoping));
        }
    }

    @EventBusSubscriber(modid = LegSpyglass.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Network {
        private Network() {
        }

        @SubscribeEvent
        static void register(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(ScopeStatePayload.TYPE, ScopeStatePayload.STREAM_CODEC, (payload, context) ->
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            set(player, payload.scoping() && hasSlotted(player));
                        }
                    }));
            registrar.playToClient(ScopeBroadcastPayload.TYPE, ScopeBroadcastPayload.STREAM_CODEC, (payload, context) ->
                    context.enqueueWork(() -> dev.qur1s.legspyglass.client.ClientScope.onBroadcast(payload)));
        }
    }

    @EventBusSubscriber(modid = LegSpyglass.MODID)
    public static final class Events {
        private Events() {
        }

        /** Looking through the spyglass ends the moment it is no longer in the slot. */
        @SubscribeEvent
        static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0
                    && SCOPING.contains(player.getUUID()) && !hasSlotted(player)) {
                set(player, false);
            }
        }

        @SubscribeEvent
        static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            SCOPING.remove(event.getEntity().getUUID());
        }
    }
}
