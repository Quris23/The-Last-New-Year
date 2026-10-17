package dev.qur1s.cosmeticsuit.mixin;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Copper/netherite backtanks run only on NorthStar oxygen: Create sees that oxygen as the tank's air
 * (netherite diving set bonus, lava/underwater diving) and Create's own air in them is ignored.
 */
@Mixin(value = BacktankUtil.class, remap = false)
public class BacktankUtilMixin {
    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    private static void cosmeticsuit$getAir(ItemStack backtank, CallbackInfoReturnable<Integer> cir) {
        if (CosmeticSuit.isOxygenBacktank(backtank)) {
            cir.setReturnValue(Math.min(CosmeticSuit.oxygenOf(backtank), BacktankUtil.maxAir(backtank)));
        }
    }

    @Inject(method = "consumeAir", at = @At("HEAD"), cancellable = true)
    private static void cosmeticsuit$consumeAir(LivingEntity entity, ItemStack backtank, int amount, CallbackInfo ci) {
        if (CosmeticSuit.isOxygenBacktank(backtank)) {
            CosmeticSuit.setOxygen(backtank, Math.max(0, CosmeticSuit.oxygenOf(backtank) - amount));
            ci.cancel();
        }
    }
}
