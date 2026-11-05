package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.FaceImplants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The Hazmat Mask is an implant only: it cannot be worn as a helmet (not by a right-click and not in the armor slot). */
@Mixin(ArmorItem.class)
public abstract class ArmorItemNoMaskMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$noWearing(Level level, Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (FaceImplants.isMask(stack)) cir.setReturnValue(InteractionResultHolder.fail(stack));
    }

    /** What the armor slot asks: the default of NeoForge's item extension, except that the mask never fits. */
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return !FaceImplants.isMask(stack) && entity.getEquipmentSlotForItem(stack) == slot;
    }
}
