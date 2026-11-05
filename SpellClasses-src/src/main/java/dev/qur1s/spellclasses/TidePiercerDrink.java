package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tide Piercer's special ability: hold the use button and the nearest block of water (a source) around the player disappears as if a bucket took it,
 * and 100 mana come back. No aiming. The client says every other tick that the button is held; the server counts the held time while water is near.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class TidePiercerDrink {
    public static final float MANA = 100.0F;
    private static final int HOLD_TICKS = 15;
    private static final int RADIUS = 4;
    private static final int FRESH_TICKS = 6;

    private static final Map<UUID, Long> LAST_HELD = new HashMap<>();
    private static final Map<UUID, Integer> CHARGE = new HashMap<>();

    private TidePiercerDrink() {
    }

    public static void held(ServerPlayer player) {
        LAST_HELD.put(player.getUUID(), player.level().getGameTime());
    }

    private static boolean holdingPiercer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("somakespells") && id.getPath().equals("tide_piercer");
    }

    /** The nearest block of water (a source) within {@link #RADIUS} blocks of the player, or null. */
    private static BlockPos nearestWater(ServerLevel level, ServerPlayer player) {
        BlockPos center = player.blockPosition();
        Vec3 from = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    FluidState fluid = level.getFluidState(cursor);
                    if (!fluid.is(FluidTags.WATER) || !fluid.isSource()) continue;
                    double d = from.distanceToSqr(cursor.getX() + 0.5, cursor.getY() + 0.5, cursor.getZ() + 0.5);
                    if (d < bestDistance) {
                        bestDistance = d;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        Long last = LAST_HELD.get(id);
        long now = player.level().getGameTime();
        if (last == null || now - last > FRESH_TICKS || !holdingPiercer(player) || !player.isAlive()) {
            CHARGE.remove(id);
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = nearestWater(level, player);
        if (pos == null) {
            CHARGE.remove(id);
            return;
        }
        int charge = CHARGE.merge(id, 1, Integer::sum);
        if (charge % 5 == 0) {
            level.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 4, 0.25, 0.1, 0.25, 0.02);
        }
        if (charge < HOLD_TICKS) return;
        CHARGE.remove(id);
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof BucketPickup pickup) {
            ItemStack taken = pickup.pickupBlock(player, level, pos, state);
            if (taken.isEmpty()) return;
            MagicData magic = MagicData.getPlayerMagicData(player);
            float max = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA);
            magic.setMana(Math.min(max, magic.getMana() + MANA));
            // Iron's syncs mana to the client only while it regenerates: at full mana nothing would ever tell the client the new value
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new io.redspace.ironsspellbooks.network.SyncManaPacket(magic));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.3, 0.1, 0.3, 0.05);
        }
    }
}
