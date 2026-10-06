package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.AegisMath;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ancient Remnants' "Sentinel Aegis" blessing does not touch the armor attribute: it adds up to +20 to the armor
 * value only inside the damage formula (LivingEntity#getDamageAfterArmorAbsorb), so no armor bar can show it. This
 * adds the same amount to the number the Overloaded Armor Bar draws, so the extra points appear as extra icons.
 * Same curve as the effect: +20 * easeInCircle(fraction), see {@link AegisMath}.
 */
@Mixin(targets = "tfar.overloadedarmorbar.overlay.OverlayRenderer", remap = false)
public abstract class OverloadedArmorBarAegisMixin {
    private static final Logger LOG = LoggerFactory.getLogger("spellclasses/aegisbar");
    private static long lastLog;

    private static final ResourceLocation SENTINEL_AEGIS =
            ResourceLocation.fromNamespaceAndPath("ancient_remnants", "sentinel_aegis");

    @Redirect(method = "renderArmorBar", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int spellclasses$withAegis(Player player) {
        int armor = player.getArmorValue();
        var effect = BuiltInRegistries.MOB_EFFECT.getHolder(SENTINEL_AEGIS);
        long now = System.currentTimeMillis();
        if (now - lastLog > 5000) {
            lastLog = now;
            LOG.info("armor bar asked for the value: attribute {}, Sentinel Aegis effect registered: {}, active: {}",
                    armor, effect.isPresent(), effect.isPresent() && player.hasEffect(effect.get()));
        }
        if (effect.isPresent() && player.hasEffect(effect.get())) {
            float missing = AegisMath.missingFraction(player.getHealth(), player.getMaxHealth());
            float eased = 1.0f - (float) Math.sqrt(1.0f - missing * missing);
            armor += Math.round(20.0f * eased);
        }
        return armor;
    }
}
