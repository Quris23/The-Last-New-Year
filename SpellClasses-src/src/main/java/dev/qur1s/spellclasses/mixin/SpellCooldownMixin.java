package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Angel Wings' per-level cooldown. Flat per-spell cooldowns (Haste, Blood Slash, ...) are set via
 * {@code config/irons_spellbooks_spell_config/irons_spellbooks/<spell>.json} instead - Iron's
 * Spellbooks reads that config directly, no code needed. But {@code cooldown_in_seconds} in that
 * config is a single flat value with no per-level scaling, so a level-dependent cooldown still
 * needs to happen here, where the caster (and their cast level) is available.
 */
@Mixin(value = MagicManager.class, remap = false)
public abstract class SpellCooldownMixin {
    private static final ResourceLocation ANGEL_WING = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "angel_wing");
    private static final int[] ANGEL_WING_COOLDOWN_SECONDS_BY_LEVEL = {100, 90, 80, 70, 60};

    @Inject(method = "getEffectiveSpellCooldown", at = @At("RETURN"), cancellable = true)
    private static void spellclasses$overrideCooldown(AbstractSpell spell, Player player, CastSource castSource, CallbackInfoReturnable<Integer> cir) {
        if (!spell.getSpellResource().equals(ANGEL_WING)) return;

        int level = MagicData.getPlayerMagicData(player).getCastingSpellLevel();
        if (level < 1) return;
        int index = Math.min(level, ANGEL_WING_COOLDOWN_SECONDS_BY_LEVEL.length) - 1;
        cir.setReturnValue(ANGEL_WING_COOLDOWN_SECONDS_BY_LEVEL[index] * 20);
    }
}
