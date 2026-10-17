package dev.qur1s.spellclasses.spell;

import dev.qur1s.spellclasses.entity.LightShieldEntity;
import dev.qur1s.spellclasses.entity.ModEntities;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/** Holy-school spell: a light-yellow sphere shield (radius 3) that follows the caster for 10 seconds. */
public class LightShieldSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("spellclasses", "light_shield");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(35)
            .build();

    public LightShieldSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 350;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.GUIDING_BOLT_CAST.get());
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", (int) LightShieldEntity.RADIUS),
                Component.translatable("ui.irons_spellbooks.effect_length", LightShieldEntity.DURATION_TICKS / 20 + "s"));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            // a recast replaces the caster's previous shield instead of stacking
            for (LightShieldEntity old : level.getEntitiesOfClass(LightShieldEntity.class,
                    entity.getBoundingBox().inflate(LightShieldEntity.RADIUS + 4))) {
                if (entity.getUUID().equals(old.ownerId())) old.discard();
            }
            LightShieldEntity shield = new LightShieldEntity(ModEntities.LIGHT_SHIELD.get(), level);
            shield.setOwner(entity);
            shield.setPos(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
            level.addFreshEntity(shield);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
