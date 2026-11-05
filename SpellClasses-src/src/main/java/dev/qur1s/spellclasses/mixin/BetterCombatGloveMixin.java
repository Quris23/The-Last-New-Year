package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.CopperGloveTweak;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Better Combat reads the main hand to pick its attack animations and reach. With an empty hand and a glove or
 * gauntlet worn in a hands slot, Better Combat is told the glove is in the hand, so the fist animations work from
 * the slot. Everyone else still sees the real, empty hand. Right click is not touched.
 */
@Mixin(LivingEntity.class)
public abstract class BetterCombatGloveMixin {
    @Inject(method = "getMainHandItem", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$gloveForBetterCombat(CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty() || !((Object) this instanceof Player player)) return;
        ItemStack glove = CopperGloveTweak.wornGlove(player);
        if (!glove.isEmpty() && CopperGloveTweak.calledByBetterCombat()) {
            cir.setReturnValue(glove);
            return;
        }
        // the Fiery Jaw's own code reads the main hand to find the jaw (cooldown, on-hit fire, ability)
        ItemStack jaw = CopperGloveTweak.wornJaw(player);
        if (!jaw.isEmpty() && CopperGloveTweak.calledByFieryJaw()) cir.setReturnValue(jaw);
    }
}
