package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.FaceImplants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** While the armor layer draws the implanted mask (see HumanoidArmorLayerMaskMixin) the head slot answers with it; at all other times nothing changes. */
@Mixin(Player.class)
public abstract class PlayerHeadForRenderMixin {
    @Inject(method = "getItemBySlot", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$renderHead(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.HEAD) return;
        ItemStack mask = FaceImplants.RENDER_HEAD.get();
        if (mask != null && cir.getReturnValue().isEmpty()) cir.setReturnValue(mask);
    }
}
