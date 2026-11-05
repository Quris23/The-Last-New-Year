package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.spells.fire.FireArrowSpell;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Magic Shield has no cooldown. Fire Arrow's cooldown is exactly 60 minutes: cooldown reduction (which Iron's applies after the base value) does not shorten it. */
@Mixin(value = MagicManager.class, remap = false)
public abstract class FireArrowCooldownMixin {
    private static final int FIRE_ARROW_COOLDOWN_TICKS = 60 * 60 * 20;

    @Inject(method = "getEffectiveSpellCooldown", at = @At("RETURN"), cancellable = true, require = 0)
    private static void spellclasses$fireArrowFixedCooldown(AbstractSpell spell, Player player, CastSource castSource,
                                                            CallbackInfoReturnable<Integer> cir) {
        if (spell instanceof FireArrowSpell) cir.setReturnValue(FIRE_ARROW_COOLDOWN_TICKS);
        else if (spell instanceof io.redspace.ironsspellbooks.spells.evocation.ShieldSpell) cir.setReturnValue(0); // Magic Shield: no cooldown at all
    }
}
