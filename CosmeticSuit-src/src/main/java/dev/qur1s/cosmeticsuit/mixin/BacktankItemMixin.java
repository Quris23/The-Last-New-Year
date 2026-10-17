package dev.qur1s.cosmeticsuit.mixin;

import com.simibubi.create.content.equipment.armor.BacktankItem;
import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Copper/netherite backtanks show NorthStar's oxygen bar only, not Create's air bar. */
@Mixin(value = BacktankItem.class, remap = false)
public class BacktankItemMixin {
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void cosmeticsuit$hideAirBar(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CosmeticSuit.isOxygenBacktank(stack)) cir.setReturnValue(false);
    }
}
