package lny.stockticker;

import com.mojang.logging.LogUtils;
import lny.stockticker.client.StockTickerClient;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Create: Mobile Packages' portable stock ticker as a Chromeware implant: worn in a hands socket (let in by SpellClasses),
 * opened with the arm hardware key, sliding out of the right wrist on the mantis blade rig, and painted with the implant
 * colours of Chromeware's customize page. Does nothing without Chromeware and Create: Mobile Packages.
 */
@Mod(StockTickerMod.MOD_ID)
public final class StockTickerMod {
   public static final String MOD_ID = "lny_stock_ticker";
   public static final Logger LOGGER = LogUtils.getLogger();

   public StockTickerMod(IEventBus modBus, Dist dist) {
      modBus.addListener(StockTickerNetwork::register);
      if (!active()) {
         return;
      }
      NeoForge.EVENT_BUS.addListener(StockTickerDeploy::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(StockTickerDeploy::onStartTracking);
      NeoForge.EVENT_BUS.addListener(StockTickerDeploy::onLogout);
      if (dist.isClient()) {
         StockTickerClient.register(modBus);
      }
   }

   /** Whether the mods the ticker implant is made of are installed */
   public static boolean active() {
      return ModList.get().isLoaded("chromeware") && ModList.get().isLoaded("create_mobile_packages");
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
   }
}
