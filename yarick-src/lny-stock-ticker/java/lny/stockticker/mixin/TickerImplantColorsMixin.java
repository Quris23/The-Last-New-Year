package lny.stockticker.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Chromeware keeps the implant colours a player chose as "implant:part" keys, and the server takes only those of its own
// implants. The portable stock ticker of the hands sockets gets colours too (drawn by StockTickerColors), so its keys are let
// through as well (applies only when Chromeware is installed)
@Pseudo
@Mixin(targets = "com.chromeware.cyberware.ImplantColors", remap = false)
public class TickerImplantColorsMixin {
   private static final List<String> lny_stock_ticker$PARTS = List.of("brass", "copper", "steel", "dark", "lights", "wire");

   @Inject(method = "valid", at = @At("HEAD"), cancellable = true, remap = false)
   private static void lny_stock_ticker$valid(String key, CallbackInfoReturnable<Boolean> cir) {
      if (key.startsWith("stock_ticker:") && lny_stock_ticker$PARTS.contains(key.substring("stock_ticker:".length()))) {
         cir.setReturnValue(true);
      }
   }
}
