package lny.octotilt;

import dev.tentaworks.client.RigClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

// Client side of RigMining: the local player's rig state, loaded only on the client
final class RigMiningClient {
   private RigMiningClient() {
   }

   static boolean carries(Player player) {
      return player == Minecraft.getInstance().player && RigClient.state().carries();
   }
}
