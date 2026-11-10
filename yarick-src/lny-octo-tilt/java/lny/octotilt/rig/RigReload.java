package lny.octotilt.rig;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

// The recoloured rig textures are made from the resource packs' own, so they are made again after a reload
public final class RigReload {
   private RigReload() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener((RegisterClientReloadListenersEvent event) ->
         event.registerReloadListener((ResourceManagerReloadListener)resources -> RigColors.clear()));
   }
}
