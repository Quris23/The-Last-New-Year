package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The Overload spell's Charged effect boosts movement speed, and vanilla's FOV widening reads the
 * live movement-speed attribute directly (AbstractClientPlayer#getFieldOfViewModifier) — so any
 * speed source (Charged, sprinting, gear, ...) zooms the camera out, and they all stack. Freeze the
 * FOV to neutral outright while Charged is active — return the player's own walking speed as
 * "current speed" whenever Charged is active, which makes the (speed/walkingSpeed + 1)/2 ratio
 * evaluate to exactly 1 (no change) for the whole duration of the buff.
 */
@Mixin(value = AbstractClientPlayer.class, remap = false)
public abstract class ChargedFovMixin {
    @Redirect(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double spellclasses$freezeFovWhileCharged(LivingEntity self, Holder<Attribute> attribute) {
        if (self instanceof Player player && self.hasEffect(MobEffectRegistry.CHARGED)) {
            return player.getAbilities().getWalkingSpeed();
        }
        return self.getAttributeValue(attribute);
    }
}
