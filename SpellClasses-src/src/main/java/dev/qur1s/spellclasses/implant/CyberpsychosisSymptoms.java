package dev.qur1s.spellclasses.implant;

import com.chromeware.humanity.Humanity;
import dev.qur1s.spellclasses.network.SymptomPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra symptoms of every cyberpsychosis attack (Chromeware's episode, which comes every few seconds and faster the lower the Humanity is).
 * Three independent rolls on every attack, each with its own chance:
 * <ul>
 * <li>an effect (nausea, blindness, silence; from 30% Humanity also wither and hunger) for 5 seconds, at 45% Humanity and lower;</li>
 * <li>a random key pressed for a second (movement, jump, sneak, drop) or dizziness (the camera turns by itself for 5 seconds; one of the
 * eight equal choices of this group), below 45%;</li>
 * <li>a mouse button or the arm hardware key pressed, below 30%.</li>
 * </ul>
 */
public final class CyberpsychosisSymptoms {
    /** Chance of each group on one attack. */
    public static final double EFFECT_CHANCE = 0.30;
    public static final double KEYS_CHANCE = 0.10;
    public static final double MOUSE_CHANCE = 0.25;

    public static final double EFFECTS_AT = 45.0;
    public static final double KEYS_BELOW = 45.0;
    public static final double MOUSE_BELOW = 30.0;
    public static final double HEAVY_EFFECTS_AT = 30.0;
    public static final int EFFECT_TICKS = 100;

    private CyberpsychosisSymptoms() {
    }

    private static double humanity(ServerPlayer player) {
        // a forced psychosis shows as 0 Humanity, though the stored value is higher
        return Humanity.psychotic(player) ? 0.0 : 100.0 - Humanity.state(player).loss();
    }

    public static void onAttack(ServerPlayer player) {
        if (player.isSpectator() || !player.isAlive()) return;
        double humanity = humanity(player);
        var random = player.getRandom();
        int mask = 0;

        if (humanity <= EFFECTS_AT && random.nextDouble() < EFFECT_CHANCE) {
            List<ResourceLocation> pool = new ArrayList<>(List.of(
                    ResourceLocation.withDefaultNamespace("nausea"),
                    ResourceLocation.withDefaultNamespace("blindness"),
                    ResourceLocation.fromNamespaceAndPath("discerning_the_eldritch", "silence_potion_effect")));
            if (humanity <= HEAVY_EFFECTS_AT) {
                pool.add(ResourceLocation.withDefaultNamespace("wither"));
                pool.add(ResourceLocation.withDefaultNamespace("hunger"));
            }
            ResourceLocation pick = pool.get(random.nextInt(pool.size()));
            BuiltInRegistries.MOB_EFFECT.getHolder(pick).ifPresent(effect ->
                    player.addEffect(new MobEffectInstance(effect, EFFECT_TICKS, pick.getPath().equals("hunger") ? 1 : 0, false, true)));
        }
        if (humanity < KEYS_BELOW && random.nextDouble() < KEYS_CHANCE) {
            // seven keys and dizziness, equal chances
            mask |= random.nextInt(8) == 0 ? SymptomPayload.DIZZY : SymptomPayload.KEYS;
        }
        if (humanity < MOUSE_BELOW && random.nextDouble() < MOUSE_CHANCE) mask |= SymptomPayload.MOUSE;
        if (mask != 0) PacketDistributor.sendToPlayer(player, new SymptomPayload(mask));
    }
}
