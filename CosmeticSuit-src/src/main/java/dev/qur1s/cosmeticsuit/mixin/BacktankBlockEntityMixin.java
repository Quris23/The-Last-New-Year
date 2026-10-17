package dev.qur1s.cosmeticsuit.mixin;

import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** A placed copper/netherite backtank never charges Create air from rotation: its only air is NorthStar oxygen. */
@Mixin(value = BacktankBlockEntity.class, remap = false)
public class BacktankBlockEntityMixin {
    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/armor/BacktankBlockEntity;getSpeed()F"))
    private float cosmeticsuit$noCharge(BacktankBlockEntity self) {
        ResourceLocation block = BuiltInRegistries.BLOCK.getKey(self.getBlockState().getBlock());
        return dev.qur1s.cosmeticsuit.CosmeticSuit.isOxygenBacktank(block) ? 0.0f : self.getSpeed();
    }
}
