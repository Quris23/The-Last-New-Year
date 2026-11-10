package lny.octotilt;

import dev.tentaworks.rig.RigServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * A player off the ground digs five times slower, and the tentacle rig always holds its wearer off the ground; while it
 * carries them they dig as if standing. Worked out on both sides alike (the server checks how long a block took to break).
 */
public final class RigMining {
   // Vanilla's airborne dig speed divisor
   private static final float AIRBORNE = 5.0F;

   private RigMining() {
   }

   public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
      Player player = event.getEntity();
      if (!player.onGround() && carries(player)) {
         event.setNewSpeed(event.getNewSpeed() * AIRBORNE);
      }
   }

   private static boolean carries(Player player) {
      if (player instanceof ServerPlayer server) {
         return RigServer.state(server).carries();
      }
      return player.level().isClientSide && FMLEnvironment.dist.isClient() && RigMiningClient.carries(player);
   }
}
