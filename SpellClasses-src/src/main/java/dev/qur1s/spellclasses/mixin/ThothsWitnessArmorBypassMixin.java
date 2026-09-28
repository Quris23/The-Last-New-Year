package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.ClassManager;
import dev.qur1s.spellclasses.NecronomiconSpells;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ThothsWitnessSpell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thoth's Witness has its own hardcoded Pharaoh-armor requirement written entirely inside its own
 * {@code canBeCastedBy} override (it never calls {@code AbstractSpell.canBeCastedBy} until after
 * the armor check passes), so {@link AbstractSpellOverridesMixin}'s necronomicon bypass - injected
 * into the base class's method - never runs for it. This intercepts the subclass directly instead.
 */
@Mixin(value = ThothsWitnessSpell.class, remap = false)
public abstract class ThothsWitnessArmorBypassMixin {
    @Inject(method = "canBeCastedBy", at = @At("HEAD"), cancellable = true)
    private void spellclasses$necronomiconBypass(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!ClassManager.chosenSchool(sp).map(SchoolRegistry.BLOOD_RESOURCE::equals).orElse(false)) return;
        if (!NecronomiconSpells.isHolding(player)) return;
        if (!NecronomiconSpells.wearsBloodArmor(player)) return;

        cir.setReturnValue(new CastResult(CastResult.Type.SUCCESS));
    }
}
