package lny.octotilt;

import com.mlh.aero_player_tilt.tilt.PlayerTilt;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

/**
 * The lean aero_player_tilt draws the player's body (and keeps the hitbox) with. Loaded only when it is installed.
 */
final class AptBody {
   private AptBody() {
   }

   // Null when it does not lean the body (switched off in its config, or the server has no aero_player_tilt)
   static Quaternionf lean(Player player, float partialTick) {
      Quaterniond lean = PlayerTilt.getRenderOrientation(player, partialTick);
      return lean == null ? null : new Quaternionf((float)lean.x, (float)lean.y, (float)lean.z, (float)lean.w);
   }
}
