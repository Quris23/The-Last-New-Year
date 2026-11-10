package lny.stockticker.mixin;

import lny.stockticker.StockTickerImplant;
import lny.stockticker.StockTickerNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The portable stock ticker in a hands socket of Chromeware (StockTickerImplant), each applying only when its mod is installed
public class TickerImplantMixins {
   // SpellClasses decides what else goes into Chromeware's sockets
   @Pseudo
   @Mixin(targets = "dev.qur1s.spellclasses.implant.ImplantUpgrades", remap = false)
   public static class Rule {
      @SuppressWarnings({"rawtypes", "unchecked"})
      @Inject(method = "rule", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$rule(ItemStack stack, CallbackInfoReturnable cir) {
         if (cir.getReturnValue() == null && StockTickerImplant.isTicker(stack)) {
            Object rule = StockTickerImplant.rule();
            if (rule != null) {
               cir.setReturnValue(rule);
            }
         }
      }

      // The slot line of the tooltip names only SpellClasses' own categories, the rest came out as the circulatory system
      @Inject(method = "categoryKey", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$hands(int category, CallbackInfoReturnable<String> cir) {
         if (category == StockTickerImplant.HANDS) {
            cir.setReturnValue("hands");
         }
      }
   }

   // The arm hardware key, on the server
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.ArmControls", remap = false)
   public static class Key {
      @Inject(method = "toggle", at = @At("HEAD"), cancellable = true, remap = false)
      private static void lny_stock_ticker$open(Player player, CallbackInfo ci) {
         if (StockTickerImplant.openInstead(player)) {
            ci.cancel();
         }
      }
   }

   // The same key on the client: the crouch key's state goes first, so the server can tell even where sneaking is not shared
   @Pseudo
   @Mixin(targets = "com.chromeware.client.cyberware.ArmInput", remap = false)
   public static class Crouch {
      @Inject(method = "toggle", at = @At("HEAD"), remap = false)
      private static void lny_stock_ticker$crouchKey(CallbackInfo ci) {
         PacketDistributor.sendToServer(new StockTickerNetwork.CrouchKey(Minecraft.getInstance().options.keyShift.isDown()));
      }
   }

   // Where the ticker's own key, its menu and its requests look for it
   @Pseudo
   @Mixin(targets = "de.theidler.create_mobile_packages.items.portable_stock_ticker.PortableStockTicker", remap = false)
   public static class Find {
      @Inject(method = "find", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$socket(Inventory inventory, CallbackInfoReturnable<ItemStack> cir) {
         ItemStack found = cir.getReturnValue();
         if (found == null || found.isEmpty()) {
            ItemStack socketed = StockTickerImplant.find(inventory.player);
            if (!socketed.isEmpty()) {
               cir.setReturnValue(socketed);
            }
         }
      }
   }
}
