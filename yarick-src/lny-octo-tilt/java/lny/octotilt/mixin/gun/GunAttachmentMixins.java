package lny.octotilt.mixin.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import lny.octotilt.gun.GunImplants;
import lny.octotilt.gun.client.GunImplantColors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Attachments fitted to the gun of a gun implant are drawn in the implant's chosen colours (GunImplantColors.attachment):
// while TaCZ draws an attachment of such a gun, the attachment's texture is answered with its recoloured copy
public class GunAttachmentMixins {
   @Pseudo
   @Mixin(targets = "com.tacz.guns.client.model.functional.AttachmentRender", remap = false)
   public static class Render {
      @Inject(method = "renderAttachment", at = @At("HEAD"), remap = false, require = 0)
      private static void octotilt$begin(ItemStack attachment, ItemStack gun, PoseStack poseStack, ItemDisplayContext context, int light, int overlay, CallbackInfo ci) {
         GunImplantColors.drawing = GunImplants.ofGun(gun);
      }

      @Inject(method = "renderAttachment", at = @At("RETURN"), remap = false, require = 0)
      private static void octotilt$end(ItemStack attachment, ItemStack gun, PoseStack poseStack, ItemDisplayContext context, int light, int overlay, CallbackInfo ci) {
         GunImplantColors.drawing = null;
      }
   }

   @Pseudo
   @Mixin(targets = "com.tacz.guns.client.resource.index.ClientAttachmentIndex", remap = false)
   public static class Texture {
      @Inject(method = "getModelTexture", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
      private void octotilt$recoloured(CallbackInfoReturnable<ResourceLocation> cir) {
         if (GunImplantColors.drawing != null) {
            cir.setReturnValue(GunImplantColors.attachment(cir.getReturnValue()));
         }
      }
   }
}
