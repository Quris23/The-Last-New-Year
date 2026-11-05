package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Ascended One (Discerning The Eldritch boss) has a mana pool: every spell it casts costs mana, mana regenerates
 * slowly, and with an empty pool it cannot cast and falls back to melee. The pool is shown as a second, blue boss bar.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class AscendedOneMana {
    private static final String TYPE_ID = "discerning_the_eldritch:ascended_one";
    private static final String KEY = "spellclasses_mana";
    private static final float MAX_MANA = 1000f;
    private static final float REGEN_PER_TICK = 25f / 20f;
    private static final double BAR_RANGE = 80.0;

    private static final Map<UUID, ServerBossEvent> BARS = new ConcurrentHashMap<>();

    private AscendedOneMana() {
    }

    private static boolean isAscendedOne(Entity e) {
        return e instanceof LivingEntity && TYPE_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
    }

    private static final net.minecraft.resources.ResourceLocation SILENCE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("discerning_the_eldritch", "silence_potion_effect");

    /** True while the creature carries the Silence effect and so cannot cast. */
    public static boolean isSilenced(LivingEntity mob) {
        if (mob.level().isClientSide) return false;
        return BuiltInRegistries.MOB_EFFECT.getHolder(SILENCE).map(mob::hasEffect).orElse(false);
    }

    private static float getMana(Entity e) {
        CompoundTag tag = e.getPersistentData();
        return tag.contains(KEY) ? tag.getFloat(KEY) : MAX_MANA;
    }

    /** Called before the boss starts casting. Returns false when it has too little mana (the cast is cancelled). */
    public static boolean tryConsume(LivingEntity mob, AbstractSpell spell, int level) {
        if (mob.level().isClientSide || !isAscendedOne(mob)) return true;
        float mana = getMana(mob);
        float cost = spell.getManaCost(level);
        if (mana < cost) return false;
        mob.getPersistentData().putFloat(KEY, mana - cost);
        return true;
    }

    @SubscribeEvent
    static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.level().isClientSide || !isAscendedOne(e) || !e.isAlive()) return;
        float mana = Math.min(MAX_MANA, getMana(e) + REGEN_PER_TICK);
        e.getPersistentData().putFloat(KEY, mana);
        // the Scales of Obedience (Weighing of Souls) weigh the real mana from Iron's own data
        if (e instanceof io.redspace.ironsspellbooks.api.entity.IMagicEntity magic && magic.getMagicData() != null)
            magic.getMagicData().setMana(mana);

        ServerBossEvent bar = BARS.computeIfAbsent(e.getUUID(), id -> new ServerBossEvent(
                Component.literal("Мана"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS));
        bar.setProgress(mana / MAX_MANA);
        if (e.tickCount % 10 == 0) {
            Set<ServerPlayer> near = new HashSet<>();
            for (ServerPlayer p : ((net.minecraft.server.level.ServerLevel) e.level()).players()) {
                if (p.distanceToSqr(e) <= BAR_RANGE * BAR_RANGE) near.add(p);
            }
            for (ServerPlayer p : new HashSet<>(bar.getPlayers())) {
                if (!near.contains(p)) bar.removePlayer(p);
            }
            for (ServerPlayer p : near) bar.addPlayer(p);
        }
    }

    @SubscribeEvent
    static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        ServerBossEvent bar = BARS.remove(event.getEntity().getUUID());
        if (bar != null) bar.removeAllPlayers();
    }
}
