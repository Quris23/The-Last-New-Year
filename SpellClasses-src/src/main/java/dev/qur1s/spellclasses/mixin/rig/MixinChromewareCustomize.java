package dev.qur1s.spellclasses.mixin.rig;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import dev.qur1s.spellclasses.client.rig.TentacleRigColors;
import dev.qur1s.spellclasses.client.rig.TentacleRigImplant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class MixinChromewareCustomize {
   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.cyberware.CustomizePage"},
      remap = false
   )
   public static class Page {
      private static List<String> spellclasses$implants;

      @Redirect(
         method = {"<init>", "renderPreview", "parts", "draw", "clickControls", "dragSlider"},
         at = @At(
            value = "FIELD",
            target = "Lcom/chromeware/cyberware/ImplantColors;IMPLANTS:Ljava/util/List;",
            opcode = 178
         ),
         remap = false
      )
      private List<String> spellclasses$withTentacleRig() {
         if (spellclasses$implants == null) {
            try {
               List<String> implants = new ArrayList<>(
                  (List)Class.forName("com.chromeware.cyberware.ImplantColors", false, MixinChromewareCustomize.Page.class.getClassLoader())
                     .getField("IMPLANTS")
                     .get(null)
               );
               implants.add("tentacle_rig");
               spellclasses$implants = List.copyOf(implants);
            } catch (ReflectiveOperationException var2) {
               spellclasses$implants = List.of("tentacle_rig");
            }
         }

         return spellclasses$implants;
      }

      @ModifyConstant(
         method = {"draw", "clickControls"},
         constant = {@Constant(
            intValue = 24,
            ordinal = 0
         )},
         remap = false
      )
      private int spellclasses$rowStep(int step) {
         return 21;
      }

      @Inject(
         method = {"implantItem"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$rigItem(String implant, CallbackInfoReturnable<ItemStack> cir) {
         if (implant.equals("tentacle_rig")) {
            cir.setReturnValue(TentacleRigColors.icon());
         }
      }

      @Inject(
         method = {"installedImplant"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private void spellclasses$rigInstalled(String implant, CallbackInfoReturnable<Boolean> cir) {
         if (implant.equals("tentacle_rig")) {
            cir.setReturnValue(TentacleRigImplant.isImplanted(Minecraft.getInstance().player));
         }
      }
   }

   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.cyberware.ImplantPreview"},
      remap = false
   )
   public static class Preview {
      @Inject(
         method = {"render"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$rigPreview(
         GuiGraphics graphics, String implant, @Coerce Object colors, float x, float y, float size, float yaw, float pitch, float seconds, CallbackInfo ci
      ) {
         if (implant.equals("tentacle_rig")) {
            TentacleRigColors.preview(graphics, colors, x, y, size, yaw, pitch, seconds);
            ci.cancel();
         }
      }

      @Inject(
         method = {"defaultYaw"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$rigYaw(String implant, CallbackInfoReturnable<Float> cir) {
         if (implant.equals("tentacle_rig")) {
            cir.setReturnValue(150.0F);
         }
      }
   }

   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.cyberware.ImplantTint"},
      remap = false
   )
   public static class Tint {
      @Inject(
         method = {"parts"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$rigParts(String implant, CallbackInfoReturnable<List<String>> cir) {
         if (implant.equals("tentacle_rig")) {
            cir.setReturnValue(TentacleRigColors.parts());
         }
      }

      @Inject(
         method = {"defaultColor"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$rigColor(String implant, String part, CallbackInfoReturnable<Integer> cir) {
         if (implant.equals("tentacle_rig")) {
            cir.setReturnValue(TentacleRigColors.defaultColor(part));
         }
      }
   }
}
