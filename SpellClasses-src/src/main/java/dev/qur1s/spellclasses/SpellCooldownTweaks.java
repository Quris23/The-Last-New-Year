package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.SpellCooldownAddedEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Per-spell cooldown overrides - Iron's Spellbooks cooldown is a single flat value per spell, with no per-level scaling built in. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class SpellCooldownTweaks {
    private SpellCooldownTweaks() {
    }

    private static final ResourceLocation ANGEL_WING = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "angel_wing");
    /** Linear 120 -> 55 sec across Angel Wings' 5 levels (step 16.25 sec), by user design. */
    private static final int[] ANGEL_WING_COOLDOWN_TICKS_BY_LEVEL = {2400, 2075, 1750, 1425, 1100};

    private static final ResourceLocation HASTE = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "haste");
    private static final int HASTE_COOLDOWN_SECONDS = 45;

    @SubscribeEvent
    static void onCooldownAdded(SpellCooldownAddedEvent.Pre event) {
        ResourceLocation spellId = event.getSpell().getSpellResource();

        if (spellId.equals(ANGEL_WING)) {
            int level = MagicData.getPlayerMagicData(event.getEntity()).getCastingSpellLevel();
            if (level < 1) return;
            int index = Math.min(level, ANGEL_WING_COOLDOWN_TICKS_BY_LEVEL.length) - 1;
            event.setEffectiveCooldown(ANGEL_WING_COOLDOWN_TICKS_BY_LEVEL[index]);
        } else if (spellId.equals(HASTE)) {
            event.setEffectiveCooldown(HASTE_COOLDOWN_SECONDS * 20);
        }
    }
}
