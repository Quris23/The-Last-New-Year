package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ice Tomb takes 3 hits to break instead of 1 — literally 3 hits, not 3 damage points. A single
 * skeleton arrow (up to ~9 damage fully charged) used to one-shot the old 3.0f health pool since
 * {@code hurt()} subtracts the raw damage amount; capping incoming damage at 1 per hit makes health
 * actually mean "hit count".
 */
@Mixin(value = IceTombEntity.class, remap = false)
public abstract class IceTombHealthMixin {
    private static final float NEW_HEALTH = 3.0f;
    private static final float MAX_DAMAGE_PER_HIT = 1.0f;

    @Shadow
    @Mutable
    private float health;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void spellclasses$raiseHealth(Level level, Entity owner, CallbackInfo ci) {
        this.health = NEW_HEALTH;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float spellclasses$capDamagePerHit(float amount) {
        return Math.min(amount, MAX_DAMAGE_PER_HIT);
    }
}
