package lny.stockticker.mixin;

import com.chromeware.cyberware.ImplantColors;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import lny.stockticker.client.StockTickerColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The portable stock ticker on Chromeware's customize page, next to its own implants: listed last, shown with the ticker item,
// the turning ticker as the preview, and the parts painted in StockTickerColors' mask. The Wither Storm port adds the tentacle
// rig to the same list (its rows then laid out by that mod for both); alone, the rows are packed closer for the eighth one to
// fit above the reset button (applies only when Chromeware is installed)
public class TickerCustomizeMixins {
   @Pseudo
   @Mixin(targets = "com.chromeware.client.cyberware.CustomizePage", remap = false, priority = 1100)
   public static class Page {
      private static List<String> lny_stock_ticker$in;
      private static List<String> lny_stock_ticker$out;

      @ModifyExpressionValue(method = {"<init>", "renderPreview", "parts", "draw", "clickControls", "dragSlider"}, at = @At(value = "FIELD", target = "Lcom/chromeware/cyberware/ImplantColors;IMPLANTS:Ljava/util/List;", opcode = Opcodes.GETSTATIC), remap = false)
      private List<String> lny_stock_ticker$withTicker(List<String> implants) {
         if (implants != lny_stock_ticker$in) {
            List<String> out = new ArrayList<>(implants);
            if (!out.contains(StockTickerColors.IMPLANT)) {
               out.add(StockTickerColors.IMPLANT);
            }
            lny_stock_ticker$out = List.copyOf(out);
            lny_stock_ticker$in = implants;
         }
         return lny_stock_ticker$out;
      }

      // rows of the implant list: 24 px apart and 20 high, 21 apart for eight (the Wither Storm port lays them out when installed)
      @ModifyConstant(method = {"draw", "clickControls"}, constant = @Constant(intValue = 24, ordinal = 0), remap = false)
      private int lny_stock_ticker$rowStep(int step) {
         return lny_stock_ticker$ownLayout() && lny_stock_ticker$out.size() >= 8 ? 21 : step;
      }

      private static boolean lny_stock_ticker$ownLayout() {
         return lny_stock_ticker$out != null && !ModList.get().isLoaded("witherstormmod");
      }

      @Inject(method = "implantItem", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$item(String implant, CallbackInfoReturnable<ItemStack> cir) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            cir.setReturnValue(StockTickerColors.icon());
         }
      }

      @Inject(method = "installedImplant", at = @At("HEAD"), cancellable = true, remap = false)
      private void lny_stock_ticker$installed(String implant, CallbackInfoReturnable<Boolean> cir) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            cir.setReturnValue(StockTickerColors.installed(Minecraft.getInstance().player));
         }
      }
   }

   @Pseudo
   @Mixin(targets = "com.chromeware.client.cyberware.ImplantPreview", remap = false)
   public static class Preview {
      @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$preview(GuiGraphics graphics, String implant, @Coerce Object colors, float x, float y, float size, float yaw, float pitch, float seconds, CallbackInfo ci) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            StockTickerColors.preview(graphics, (ImplantColors)colors, x, y, size, yaw, pitch);
            ci.cancel();
         }
      }

      // the ticker's screen a little to the side
      @Inject(method = "defaultYaw", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$yaw(String implant, CallbackInfoReturnable<Float> cir) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            cir.setReturnValue(25.0F);
         }
      }
   }

   @Pseudo
   @Mixin(targets = "com.chromeware.client.cyberware.ImplantTint", remap = false)
   public static class Tint {
      @Inject(method = "parts", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$parts(String implant, CallbackInfoReturnable<List<String>> cir) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            cir.setReturnValue(StockTickerColors.parts());
         }
      }

      @Inject(method = "defaultColor", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$color(String implant, String part, CallbackInfoReturnable<Integer> cir) {
         if (implant.equals(StockTickerColors.IMPLANT)) {
            cir.setReturnValue(StockTickerColors.defaultColor(part));
         }
      }
   }
}
