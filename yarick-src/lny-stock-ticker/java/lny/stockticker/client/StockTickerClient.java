package lny.stockticker.client;

import lny.stockticker.StockTickerArm;
import lny.stockticker.StockTickerNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** The client side: the ticker's arm model, its recoloured textures dropped on resource reload, the wrists shown */
public final class StockTickerClient {
   private StockTickerClient() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener(StockTickerArmRenderer::registerModel);
      modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener)resources -> StockTickerColors.clear()));
      NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> StockTickerArm.clear());
   }

   public static void arm(StockTickerNetwork.Arm message) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.getEntity(message.id()) instanceof Player player) {
         StockTickerArm.show(player, message.left() ? HumanoidArm.LEFT : HumanoidArm.RIGHT, message.out());
      }
   }
}
