package dev.qur1s.cosmeticsuit.mixin;

import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.lightning.northstar.client.renderer.armor.SpaceSuitFirstPersonRenderer", remap = false)
public class SpaceSuitFirstPersonMixin {
    @Redirect(method = "onRenderPlayerHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getArmor(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack cosmeticsuit$chest(Inventory inventory, int slot) {
        return CosmeticSuit.visual(inventory.player, EquipmentSlot.CHEST);
    }
}
