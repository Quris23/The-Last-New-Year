package dev.qur1s.cosmeticsuit.mixin;

import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.lightning.northstar.world.temperature.NorthstarTemperature", remap = false)
public class NorthstarTemperatureMixin {
    @Redirect(method = {"hasInsulation", "hasHeatProtection"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack cosmeticsuit$slot(LivingEntity entity, EquipmentSlot slot) {
        return CosmeticSuit.effective(entity, slot);
    }
}
