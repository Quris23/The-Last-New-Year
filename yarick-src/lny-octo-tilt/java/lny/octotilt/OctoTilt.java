package lny.octotilt;

import dev.tentaworks.client.RigClient;
import dev.tentaworks.compat.ShipFrames;
import dev.tentaworks.rig.Grip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The lean of the local player while the Create: Octo Arms tentacle rig carries them: "up" is the average of the gripped
 * faces' normals (on a ship, turned with it), so tentacles on the floor and a wall lean halfway to the wall. Worked out once
 * per frame; the camera ({@link OctoArmsCameraTilt}) and, with aero_player_tilt, the body and hitbox take the same rotation.
 * On letting go it eases back upright. Standing still on the rig, the lean stays as it is.
 */
public final class OctoTilt {
   // Part of the angle to the averaged normal the lean follows, and the most it leans
   private static final float STRENGTH = 0.6F;
   private static final float MAX_ANGLE = (float)Math.toRadians(50.0);
   // Ticks to get halfway to a new lean
   private static final float HALF_LIFE = 3.3F;
   // Ticks the last lean is kept while the rig holds on with fewer than two tentacles mid-step
   private static final float GRACE = 6.0F;
   // Blocks per tick below which a player with no movement keys held counts as standing still
   private static final double STILL = 0.02;
   private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);
   private static final Quaternionf TARGET = new Quaternionf();
   private static final Quaternionf CURRENT = new Quaternionf();
   private static float lost = GRACE;
   private static boolean engaged;

   private OctoTilt() {
   }

   static void onRenderFrame(RenderFrameEvent.Pre event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      float delta = Math.min(mc.getTimer().getRealtimeDeltaTicks(), 5.0F);
      boolean riding = player == null || player.getVehicle() != null;
      if (!riding && engaged && RigClient.state().carries() && still(player)) {
         // Standing still on the tentacles: the lean is kept as it is. Worked out from the grips, it crept further and
         // further while the rig shifted its hold in place, then jumped back upright when the hold dropped below two
         lost = 0.0F;
      } else {
         aim(riding ? null : gripUp(player), delta);
      }
      CURRENT.slerp(TARGET, 1.0F - (float)Math.pow(0.5, delta / HALF_LIFE)).normalize();
      if (lost > GRACE && angle(CURRENT) < 0.001F) {
         CURRENT.identity();
         engaged = false;
      } else {
         engaged = true;
      }
   }

   // No movement keys held and (almost) no move since the last tick
   private static boolean still(LocalPlayer player) {
      if (player.input != null && (player.input.forwardImpulse != 0.0F || player.input.leftImpulse != 0.0F || player.input.jumping || player.input.shiftKeyDown)) {
         return false;
      }
      double dx = player.getX() - player.xo;
      double dy = player.getY() - player.yo;
      double dz = player.getZ() - player.zo;
      return dx * dx + dy * dy + dz * dz < STILL * STILL;
   }

   // The lean to ease to: towards the grips' "up", or upright once the rig has held on with fewer than two tentacles for a while
   private static void aim(Vector3f up, float delta) {
      if (up != null) {
         lost = 0.0F;
         TARGET.rotationTo(UP, up);
         float angle = angle(TARGET);
         if (angle > 1.0E-6F) {
            TARGET.set(new Quaternionf().slerp(TARGET, Math.min(angle * STRENGTH, MAX_ANGLE) / angle));
         }
      } else if ((lost += delta) > GRACE) {
         TARGET.identity();
      }
   }

   // Angle of a rotation, the short way round
   private static float angle(Quaternionf q) {
      return 2.0F * (float)Math.acos(Math.min(1.0F, Math.abs(q.w())));
   }

   /** Whether the rig leans the player now (holding on, or still easing back upright) */
   public static boolean engaged() {
      return engaged;
   }

   /** World space rotation of the lean, from upright */
   public static Quaternionf current() {
      return new Quaternionf(CURRENT);
   }

   // Averaged normal of the gripped faces while the rig carries the player on at least two tentacles; null otherwise, or
   // when the faces cancel out or point down (ceiling), which has no side to lean to
   private static Vector3f gripUp(LocalPlayer player) {
      if (!RigClient.state().carries() || RigClient.heldCount() < 2) {
         return null;
      }
      Level level = player.level();
      Vec3 sum = Vec3.ZERO;
      for (Grip grip : RigClient.gait().grips()) {
         if (grip != null) {
            Vec3 normal = Vec3.atLowerCornerOf(grip.face().getNormal());
            Vec3 base = grip.world(level);
            sum = sum.add(ShipFrames.get().toWorld(level, grip.block(), grip.point().add(normal)).subtract(base));
         }
      }
      if (sum.lengthSqr() < 1.0E-4) {
         return null;
      }
      Vec3 up = sum.normalize();
      return up.y < -0.5 ? null : new Vector3f((float)up.x, (float)up.y, (float)up.z);
   }
}
