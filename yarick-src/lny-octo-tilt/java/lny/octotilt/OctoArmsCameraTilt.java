package lny.octotilt;

import com.playsi.aero_cam_sync.api.AeroCamSyncApi;
import com.playsi.aero_cam_sync.api.TiltContext;
import com.playsi.aero_cam_sync.api.TiltSource;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;

/**
 * Leans the camera with the tentacle rig ({@link OctoTilt}), the way Aeronautics Camera Sync leans it on an uneven Sable
 * ship deck. The body and hitbox (aero_player_tilt, see the mixin) take the very same rotation, so the view stays on them.
 */
public final class OctoArmsCameraTilt implements TiltSource {
   // Above aero_player_tilt's body tilt (100), which would otherwise take the frame once the body leans
   private static final int PRIORITY = 150;

   private OctoArmsCameraTilt() {
   }

   public static void register() {
      AeroCamSyncApi.forMod(OctoTiltMod.MOD_ID).addTiltSource(PRIORITY, new OctoArmsCameraTilt());
   }

   @Override
   public boolean appliesTo(TiltContext context) {
      return context.player() == Minecraft.getInstance().player && context.player().getVehicle() == null && OctoTilt.engaged();
   }

   // The body's own lean when aero_player_tilt leans it (it may scale the lean down by its tilt multiplier), so the view
   // matches the body and hitbox exactly; the rig's lean otherwise
   @Override
   public Quaternionf tilt(TiltContext context) {
      Quaternionf body = OctoTiltMod.BODY ? AptBody.lean(context.player(), context.partialTick()) : null;
      return body != null ? body : OctoTilt.current();
   }
}
