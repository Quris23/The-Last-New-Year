package dev.qur1s.spellclasses.compat;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.raptorzizi.wind_spellbooks.entity.spells.almighty_push.AlmightyPushEntity;
import net.raptorzizi.wind_spellbooks.spells.wind.AlmightyPushSpell;

/**
 * Casting Shockwave also releases Wind's Spellbooks' Almighty Push at the caster, with its radius scaled down.
 * The spell itself is disabled in the config, so only its numbers (strength, duration) are used here.
 */
public final class WindPushOnShockwave {
    private static final ResourceLocation ALMIGHTY_PUSH = ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "almighty_push");

    private WindPushOnShockwave() {
    }

    public static void spawn(Level level, int spellLevel, LivingEntity caster, float shockwaveRadius, float radiusScale) {
        if (level.isClientSide || !ModList.get().isLoaded("wind_spellbooks")) {
            return;
        }
        AbstractSpell spell = SpellRegistry.getSpell(ALMIGHTY_PUSH);
        if (!(spell instanceof AlmightyPushSpell push)) {
            return;
        }
        AlmightyPushEntity entity = new AlmightyPushEntity(level, caster);
        entity.setRadius(shockwaveRadius * radiusScale);
        entity.strength = push.getStrength(spellLevel, caster);
        entity.amplifier = spellLevel - 1;
        entity.setLifetime(push.getDuration(spellLevel));
        level.addFreshEntity(entity);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.WIND_CHARGE_BURST.value(),
                caster.getSoundSource(), 3.0f, 0.7f);
    }
}
