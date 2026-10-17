package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A player holding {@value #MANA_THRESHOLD}+ mana gives off the same school-rune particles that Create: Wizardry
 * shows for a mana siphon / leaking mana pipe. The particle types are looked up by id, so nothing happens when
 * Create: Wizardry is not installed.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class ManaAuraParticles {
    private ManaAuraParticles() {
    }

    private static final float MANA_THRESHOLD = 1200f;
    private static final int PERIOD_TICKS = 4;
    private static final int COUNT = 2;
    private static final double SPREAD_XZ = 0.4;
    private static final double SPREAD_Y = 0.7;
    private static final double SPEED = 0.02;
    private static final String[] RUNES = {
            "arcane", "blood", "ender", "evocation", "fire", "holy", "ice", "lightning", "nature"
    };

    private static List<ParticleOptions> particles;

    private static List<ParticleOptions> particles() {
        if (particles == null) {
            List<ParticleOptions> found = new ArrayList<>();
            for (String rune : RUNES) {
                ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE
                        .getOptional(ResourceLocation.fromNamespaceAndPath("create_wizardry", rune + "_rune")).orElse(null);
                if (type instanceof ParticleOptions options) found.add(options);
            }
            particles = found;
        }
        return particles;
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % PERIOD_TICKS != 0 || player.isSpectator()) return;
        float mana = MagicData.getPlayerMagicData(player).getMana();
        if (mana < MANA_THRESHOLD) return;

        List<ParticleOptions> list = particles();
        if (list.isEmpty()) return;
        for (int i = 0; i < COUNT; i++) {
            ParticleOptions particle = list.get(player.getRandom().nextInt(list.size()));
            MagicManager.spawnParticles(player.level(), particle,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    1, SPREAD_XZ, SPREAD_Y, SPREAD_XZ, SPEED, false);
        }
    }
}
