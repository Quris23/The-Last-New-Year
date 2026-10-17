package dev.qur1s.cosmeticsuit.mixin;

import dev.qur1s.cosmeticsuit.CosmeticSuit;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Punchy draws the first-person armor sleeve itself, from the real chest slot; use the cosmetic one instead. */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public class PunchyArmArmorMixin {
    @Redirect(method = "renderArmArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack cosmeticsuit$chest(AbstractClientPlayer player, EquipmentSlot slot) {
        ItemStack stack = CosmeticSuit.visual(player, slot);
        // Backtanks and custom-modelled full-sleeve robes have no flat vanilla arm texture;
        // Punchy clones the vanilla armor-layer arm and paints it with one, so it comes out black.
        return CosmeticSuit.isPunchySleeveUnsafe(stack, player) ? ItemStack.EMPTY : stack;
    }
}
