package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.qur1s.spellclasses.implant.FaceImplants;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Hazmat Mask implanted in the Face group is drawn on the head like a worn helmet, when no real helmet is worn. After the layer
 * has drawn the armor, the head piece is drawn once more with the mask handed to it for exactly that call (drawing only).
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMaskMixin {
    @Shadow
    @SuppressWarnings("rawtypes")
    protected abstract void renderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, LivingEntity entity, EquipmentSlot slot,
                                             int light, HumanoidModel model);

    @Shadow
    @SuppressWarnings("rawtypes")
    protected abstract HumanoidModel getArmorModel(EquipmentSlot slot);

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("TAIL"), require = 0)
    @SuppressWarnings("unchecked")
    private void spellclasses$implantMask(PoseStack pose, MultiBufferSource buffer, int light, LivingEntity entity,
                                          float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                                          float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !net.neoforged.fml.ModList.get().isLoaded("chromeware")) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return;
        ItemStack mask = FaceImplants.find(player, FaceImplants::isMask);
        if (mask.isEmpty()) return;
        FaceImplants.RENDER_HEAD.set(mask);
        try {
            this.renderArmorPiece(pose, buffer, entity, EquipmentSlot.HEAD, light, this.getArmorModel(EquipmentSlot.HEAD));
        } finally {
            FaceImplants.RENDER_HEAD.remove();
        }
    }
}
