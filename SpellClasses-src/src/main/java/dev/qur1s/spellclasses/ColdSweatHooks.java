package dev.qur1s.spellclasses;

import com.momosoftworks.coldsweat.api.temperature.modifier.SimpleTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;
import java.util.Set;

/**
 * Only ever entered through {@link ColdSweatCompat} once Cold Sweat is confirmed loaded.
 * <p>
 * Cold Sweat never gives {@code EntityType.PLAYER} a real vanilla Attribute instance for
 * cold_resistance/heat_resistance (its own attribute-registration pass explicitly skips
 * players) — so those have to be granted as a {@link SimpleTempModifier} on the trait instead,
 * the same mechanism Cold Sweat itself uses for gear-based insulation. The spell-power buff
 * below, by contrast, uses Iron's Spellbooks' own attributes directly — those work fine on
 * players, so no such workaround is needed there.
 */
final class ColdSweatHooks {
    private ColdSweatHooks() {
    }

    private static final ResourceLocation POWER_BUFF_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("spellclasses", "class_environment_power");
    private static final double POWER_BUFF_AMOUNT = 0.1;

    static void syncResistance(ServerPlayer player, Optional<ResourceLocation> school) {
        Temperature.removeModifiers(player, Temperature.Trait.COLD_RESISTANCE, SimpleTempModifier.class);
        Temperature.removeModifiers(player, Temperature.Trait.HEAT_RESISTANCE, SimpleTempModifier.class);
        school.ifPresent(s -> {
            switch (s.getPath()) {
                case "ice" -> grantFullResistance(player, Temperature.Trait.COLD_RESISTANCE);
                case "fire" -> grantFullResistance(player, Temperature.Trait.HEAT_RESISTANCE);
                default -> {
                }
            }
        });
    }

    private static void grantFullResistance(ServerPlayer player, Temperature.Trait trait) {
        Temperature.addModifier(player, new SimpleTempModifier(1.0, SimpleTempModifier.Operation.SET), trait, Placement.LAST);
    }

    /** Biomes that never get winter (Serene Seasons' warm/tropical set) — see seasons.toml. */
    private static final Set<ResourceLocation> WARM_BIOMES = Set.of(
            ResourceLocation.withDefaultNamespace("savanna"),
            ResourceLocation.withDefaultNamespace("savanna_plateau"),
            ResourceLocation.withDefaultNamespace("windswept_savanna"),
            ResourceLocation.withDefaultNamespace("mangrove_swamp"),
            ResourceLocation.withDefaultNamespace("jungle"),
            ResourceLocation.withDefaultNamespace("sparse_jungle"),
            ResourceLocation.withDefaultNamespace("bamboo_jungle"),
            ResourceLocation.withDefaultNamespace("mushroom_fields"),
            ResourceLocation.withDefaultNamespace("warm_ocean")
    );

    /** Whether {@code school}'s matching environment currently holds — drives both the weak regen and the +10% spell power. */
    static boolean matchesEnvironment(ServerPlayer player, ResourceLocation school) {
        Level level = player.level();
        return switch (school.getPath()) {
            case "ice" -> Temperature.get(player, Temperature.Trait.WORLD) < 0.0;
            case "fire" -> level.dimension() == Level.NETHER;
            case "lightning" -> level.isThundering();
            case "nature" -> level.dimension() == Level.OVERWORLD && isInWarmBiome(player);
            default -> false;
        };
    }

    private static boolean isInWarmBiome(ServerPlayer player) {
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        return biome.unwrapKey().map(key -> WARM_BIOMES.contains(key.location())).orElse(false);
    }

    /** Adds/removes the +10% spell power modifier for {@code school}'s own power attribute to match {@code active}. */
    static void syncPowerBuff(ServerPlayer player, ResourceLocation school, boolean active) {
        powerAttribute(school).ifPresent(holder -> {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance == null) return;
            boolean present = instance.getModifier(POWER_BUFF_MODIFIER_ID) != null;
            if (active && !present) {
                instance.addTransientModifier(new AttributeModifier(POWER_BUFF_MODIFIER_ID, POWER_BUFF_AMOUNT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (!active && present) {
                instance.removeModifier(POWER_BUFF_MODIFIER_ID);
            }
        });
    }

    private static Optional<Holder<Attribute>> powerAttribute(ResourceLocation school) {
        return switch (school.getPath()) {
            case "ice" -> Optional.of(AttributeRegistry.ICE_SPELL_POWER);
            case "fire" -> Optional.of(AttributeRegistry.FIRE_SPELL_POWER);
            case "lightning" -> Optional.of(AttributeRegistry.LIGHTNING_SPELL_POWER);
            case "nature" -> Optional.of(AttributeRegistry.NATURE_SPELL_POWER);
            default -> Optional.empty();
        };
    }
}
