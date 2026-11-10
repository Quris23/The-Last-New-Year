package lny.octotilt.mixin.rig;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lny.octotilt.gun.GunImplants;
import lny.octotilt.gun.client.GunImplantColors;
import lny.octotilt.rig.RigColors;
import lny.octotilt.rig.RigImplant;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CustomizeMixin {
   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.cyberware.CustomizePage"},
      remap = false
   )
   public static class Page {
      private static List<String> octotilt$implants;
      private static int octotilt$scroll;
      private static int octotilt$count;
      private static Field octotilt$screenField;
      private static Method octotilt$sx;
      private static Method octotilt$sy;
      private static boolean octotilt$clipped;

      @Redirect(
         method = {"<init>", "renderPreview", "parts", "draw", "clickControls", "dragSlider"},
         at = @At(
            value = "FIELD",
            target = "Lcom/chromeware/cyberware/ImplantColors;IMPLANTS:Ljava/util/List;",
            opcode = 178
         ),
         remap = false
      )
      private List<String> octotilt$withTentacleRig() {
         if (octotilt$implants == null) {
            try {
               List<String> implants = new ArrayList<>(
                  (List)Class.forName("com.chromeware.cyberware.ImplantColors", false, CustomizeMixin.Page.class.getClassLoader())
                     .getField("IMPLANTS")
                     .get(null)
               );
               implants.add("tentacle_rig");
               if (GunImplants.active()) {
                  implants.add(GunImplants.CRANE);
               }
               octotilt$implants = List.copyOf(implants);
            } catch (ReflectiveOperationException var21) {
               octotilt$implants = List.of("tentacle_rig");
            }
         }

         return octotilt$implants;
      }

      @ModifyConstant(
         method = {"draw", "clickControls"},
         constant = {@Constant(
            intValue = 24,
            ordinal = 0
         )},
         remap = false
      )
      private int octotilt$rowStep(int step) {
         return 24;
      }

      private static int octotilt$maxScroll() {
         return Math.max(0, (octotilt$count - 1) * 24 - 146);
      }

      @ModifyConstant(
         method = {"draw", "clickControls"},
         constant = {@Constant(
            intValue = 46,
            ordinal = 0
         )},
         remap = false
      )
      private int octotilt$listTop(int top) {
         octotilt$scroll = Math.max(0, Math.min(octotilt$scroll, octotilt$maxScroll()));
         return top - octotilt$scroll;
      }

      @WrapOperation(
         method = {"draw"},
         at = {@At(
            value = "INVOKE",
            target = "Ljava/util/List;size()I",
            ordinal = 0
         )},
         remap = false
      )
      private int octotilt$count(List<?> list, Operation<Integer> original) {
         int n = (Integer)original.call(new Object[]{list});
         octotilt$count = n;

         RigImplant.shareRows(list);

         return n;
      }

      @WrapOperation(
         method = {"draw", "clickControls"},
         at = {@At(
            value = "INVOKE",
            target = "Lcom/chromeware/client/cyberware/CyberwareScreen;inside(DDIIII)Z",
            ordinal = 0
         )},
         remap = false
      )
      private static boolean octotilt$rowVisible(double mx, double my, int a, int b, int c, int d, Operation<Boolean> original) {
         return (Boolean)original.call(new Object[]{mx, my, a, b, c, d}) && my >= 44.0 && my <= 213.0;
      }

      @Inject(
         method = {"draw"},
         at = {@At(
            value = "INVOKE",
            target = "Lcom/chromeware/client/cyberware/CyberwareScreen;title(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/network/chat/Component;IIZ)V",
            shift = Shift.AFTER,
            ordinal = 0
         )},
         remap = false
      )
      private void octotilt$clipOn(GuiGraphics g, double mx, double my, CallbackInfoReturnable<List<Component>> cir) {
         try {
            if (octotilt$screenField == null) {
               octotilt$screenField = this.getClass().getDeclaredField("screen");
               octotilt$screenField.setAccessible(true);
            }

            Object screen = octotilt$screenField.get(this);
            if (octotilt$sx == null) {
               octotilt$sx = screen.getClass().getDeclaredMethod("sx", double.class);
               octotilt$sy = screen.getClass().getDeclaredMethod("sy", double.class);
               octotilt$sx.setAccessible(true);
               octotilt$sy.setAccessible(true);
            }

            g.enableScissor(
               (int)((Float)octotilt$sx.invoke(screen, 11.0)).floatValue(),
               (int)((Float)octotilt$sy.invoke(screen, 44.0)).floatValue(),
               (int)((Float)octotilt$sx.invoke(screen, 121.0)).floatValue(),
               (int)((Float)octotilt$sy.invoke(screen, 213.0)).floatValue()
            );
            octotilt$clipped = true;
         } catch (ReflectiveOperationException var8) {
            octotilt$clipped = false;
         }
      }

      @Inject(
         method = {"draw"},
         at = {@At(
            value = "INVOKE",
            target = "Lcom/chromeware/client/cyberware/PonderUi;box(Lnet/minecraft/client/gui/GuiGraphics;FFIILnet/createmod/catnip/data/Couple;Lnet/createmod/catnip/theme/Color;I)V",
            ordinal = 1
         )},
         remap = false
      )
      private void octotilt$clipOff(GuiGraphics g, double mx, double my, CallbackInfoReturnable<List<Component>> cir) {
         if (octotilt$clipped) {
            g.disableScissor();
            octotilt$clipped = false;
         }
      }

      @ModifyConstant(
         method = {"draw", "clickControls"},
         constant = {@Constant(
            intValue = 22,
            ordinal = 1
         )},
         remap = false
      )
      private int octotilt$partStep(int step) {
         return GunImplantColors.compactRows ? 17 : step;
      }

      // the reset buttons clear Chromeware's own part names; the gun implant's are added
      @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
         method = {"clickControls"},
         at = {@At(
            value = "FIELD",
            target = "Lcom/chromeware/cyberware/ImplantColors;PARTS:Ljava/util/List;",
            opcode = 178
         )},
         remap = false
      )
      private List<String> octotilt$resetParts(List<String> parts) {
         return GunImplants.withGunParts(parts);
      }

      @ModifyConstant(
         method = {"editorY"},
         constant = {@Constant(
            intValue = 22,
            ordinal = 0
         )},
         remap = false
      )
      private int octotilt$editorStep(int step) {
         return GunImplantColors.compactRows ? 17 : step;
      }

      @Inject(
         method = {"scroll"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private void octotilt$listScroll(double mx, double my, double dy, CallbackInfoReturnable<Boolean> cir) {
         int max = octotilt$maxScroll();
         if (max > 0 && mx >= 12.0 && mx <= 120.0 && my >= 44.0 && my <= 213.0) {
            octotilt$scroll = Math.max(0, Math.min(max, octotilt$scroll - (int)Math.signum(dy) * 24));
            cir.setReturnValue(true);
         }
      }

      @Inject(
         method = {"implantItem"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void octotilt$rigItem(String implant, CallbackInfoReturnable<ItemStack> cir) {
         if (implant.equals(GunImplants.CRANE)) {
            cir.setReturnValue(GunImplantColors.icon());
            return;
         }
         if (RigColors.isRigRow(implant)) {
            cir.setReturnValue(RigColors.icon(RigImplant.isNetherite(Minecraft.getInstance().player)));
         }
      }

      @Inject(
         method = {"installedImplant"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private void octotilt$rigInstalled(String implant, CallbackInfoReturnable<Boolean> cir) {
         if (implant.equals(GunImplants.CRANE)) {
            cir.setReturnValue(GunImplantColors.installed(Minecraft.getInstance().player));
            return;
         }
         if (RigColors.isRigRow(implant)) {
            cir.setReturnValue(RigImplant.isImplanted(Minecraft.getInstance().player));
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
      private static void octotilt$rigPreview(
         GuiGraphics graphics, String implant, @Coerce Object colors, float x, float y, float size, float yaw, float pitch, float seconds, CallbackInfo ci
      ) {
         if (implant.equals(GunImplants.CRANE)) {
            GunImplantColors.preview(graphics, colors, x, y, size, yaw, pitch);
            ci.cancel();
            return;
         }
         if (RigColors.isRigRow(implant)) {
            RigColors.preview(graphics, colors, x, y, size, yaw, pitch, seconds, RigImplant.isNetherite(Minecraft.getInstance().player));
            ci.cancel();
         }
      }

      @Inject(
         method = {"defaultYaw"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void octotilt$rigYaw(String implant, CallbackInfoReturnable<Float> cir) {
         if (implant.equals(GunImplants.CRANE)) {
            cir.setReturnValue(35.0F);
            return;
         }
         if (RigColors.isRigRow(implant)) {
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
      private static void octotilt$rigParts(String implant, CallbackInfoReturnable<List<String>> cir) {
         GunImplantColors.compactRows = implant.equals(GunImplants.CRANE);
         if (GunImplantColors.compactRows) {
            cir.setReturnValue(GunImplantColors.parts());
            return;
         }
         if (RigColors.isRigRow(implant)) {
            cir.setReturnValue(RigColors.parts());
         }
      }

      @Inject(
         method = {"defaultColor"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void octotilt$rigColor(String implant, String part, CallbackInfoReturnable<Integer> cir) {
         if (implant.equals(GunImplants.CRANE)) {
            cir.setReturnValue(GunImplantColors.defaultColor(part));
            return;
         }
         if (RigColors.isRigRow(implant)) {
            cir.setReturnValue(RigColors.defaultColor(part));
         }
      }
   }
}
