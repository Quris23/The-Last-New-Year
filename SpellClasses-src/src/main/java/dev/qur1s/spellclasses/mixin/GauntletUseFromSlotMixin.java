package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.CopperGloveTweak;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Fiery Jaw is used with right click. With both hands empty and the jaw worn in a hands slot, the game is told the
 * jaw is the item in the main hand, so right click uses it from the slot: the same stack instance is returned each
 * time. An item in either hand is never replaced.
 */
@Mixin(LivingEntity.class)
public abstract class GauntletUseFromSlotMixin {
    @Inject(method = "getItemInHand", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$jawFromSlot(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (hand != InteractionHand.MAIN_HAND || !cir.getReturnValue().isEmpty() || !((Object) this instanceof Player player)) return;
        ItemStack jaw = CopperGloveTweak.wornJaw(player);
        if (!jaw.isEmpty() && player.getOffhandItem().isEmpty() && !CopperGloveTweak.calledFromRendering()) cir.setReturnValue(jaw);
    }
}
