package dev.qur1s.cosmeticsuit.mixin;

import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.lightning.northstar.world.oxygen.NorthstarOxygen", remap = false)
public class NorthstarOxygenMixin {
    @Redirect(method = {"getOxygenTank", "onBreathe"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorSlots()Ljava/lang/Iterable;"))
    private static Iterable<ItemStack> cosmeticsuit$armor(LivingEntity entity) {
        return CosmeticSuit.effectiveArmor(entity);
    }
}
