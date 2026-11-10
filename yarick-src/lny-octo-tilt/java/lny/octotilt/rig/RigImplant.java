package lny.octotilt.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import lny.octotilt.OctoTiltMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The tentacle rig implanted through Spell Classes (its OctoRigBackSlot, reached by reflection): drawn on the back without
 * its tank, and in first person where the body is.
 */
public class RigImplant {
   private static final Logger LOGGER = LogManager.getLogger("lny_octo_tilt");
   private static final float BACK_OFFSET = -0.234375F;
   private static final float SCALE = 0.9F;
   private static final float DROP = 0.28968748F;
   private static final float PIVOT_X = 0.5F;
   private static final float PIVOT_Y = 0.625F;
   private static final float PIVOT_Z = 0.359375F;
   private static final float INWARD = 2.2222223F;
   private static final float RAISE_LOWER = 1.1111112F;
   private static final String[] JOINTS = new String[]{"SHOULDER", "FOLD_ELBOW", "FOLD_WRIST"};
   private static Method installedRig;
   private static boolean missing;
   private static Vector3f[][] joints;
   private static Vector3f[][] originalJoints;
   private static boolean jointsShifted;
   private static boolean jointsMissing;

   private static Method drop;
   private static boolean dropMissing;
   private static Field rowsField;
   private static boolean rowsMissing;

   public static ItemStack installedRig(LivingEntity entity) {
      if (!missing && entity != null) {
         try {
            if (installedRig == null) {
               installedRig = Class.forName("dev.qur1s.spellclasses.OctoRigBackSlot", false, RigImplant.class.getClassLoader())
                  .getMethod("installedRig", LivingEntity.class);
            }

            return (ItemStack)installedRig.invoke(null, entity);
         } catch (ReflectiveOperationException | LinkageError var2) {
            missing = true;
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   // Octo Arms draws the first person rig at the feet of an upright, standing player. Chromeware lowers the body of a player
   // without legs (and their eyes with it), and aero_player_tilt leans the body around the eyes, where the camera leans as
   // well: both are put in, so the tentacles leave the back as they do in third person and do not stick out of the head
   public static void firstPerson(PoseStack poseStack, Player player, Vec3 camera, float partialTick) {
      Quaternionf lean = player.getSwimAmount(partialTick) > 0.0F ? null : OctoTiltMod.bodyLean(player, partialTick);
      if (lean != null) {
         // The pose stack may hold the view's rotation before Octo Arms' own steps, so the lean (around the eyes, relative to
         // the camera) goes in after them, taken into their space: the feet, the body's yaw, the flip and scale, the drop
         Vec3 feet = player.getPosition(partialTick).subtract(camera);
         Vec3 eye = player.getEyePosition(partialTick).subtract(camera);
         Matrix4f body = new Matrix4f().translation((float)feet.x, (float)feet.y, (float)feet.z)
            .rotateY((float)Math.toRadians(180.0F - Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot)))
            .scale(-0.9375F, -0.9375F, 0.9375F)
            .translate(0.0F, -1.501F, 0.0F);
         Matrix4f leaned = new Matrix4f().translation((float)eye.x, (float)eye.y, (float)eye.z).rotate(lean)
            .translate((float)-eye.x, (float)-eye.y, (float)-eye.z);
         Matrix4f local = new Matrix4f(body).invert().mul(leaned).mul(body);
         PoseStack.Pose last = poseStack.last();
         last.pose().mul(local);
         last.normal().mul(local.normal(new Matrix3f()));
      }
      float lowered = chromewareDrop(player);
      if (lowered != 0.0F) {
         // y points down here, in units of 0.9375 blocks
         poseStack.translate(0.0F, lowered / 0.9375F, 0.0F);
      }
   }

   // How far Chromeware lowers the body of a player without legs (0 when it does not)
   public static float chromewareDrop(Player player) {
      if (dropMissing || player == null) {
         return 0.0F;
      }
      try {
         if (drop == null) {
            drop = Class.forName("com.chromeware.body.BodyGeometry", false, RigImplant.class.getClassLoader()).getMethod("drop", Player.class);
         }
         return (Float)drop.invoke(null, player);
      } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
         dropMissing = true;
         return 0.0F;
      }
   }

   // Spell Classes types colour values into the customize page by its rows (ColorValueEdit), told them while it is drawn
   public static void shareRows(List<?> rows) {
      if (rowsMissing) {
         return;
      }
      try {
         if (rowsField == null) {
            rowsField = Class.forName("dev.qur1s.spellclasses.client.ColorValueEdit", false, RigImplant.class.getClassLoader()).getField("implants");
         }
         rowsField.set(null, rows);
      } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
         rowsMissing = true;
      }
   }

   public static boolean isImplanted(LivingEntity entity) {
      return !installedRig(entity).isEmpty();
   }

   public static boolean isNetherite(ItemStack rig) {
      return !rig.isEmpty() && BuiltInRegistries.ITEM.getKey(rig.getItem()).getPath().startsWith("netherite");
   }

   public static boolean isImplanted(LivingEntity entity, boolean netherite) {
      ItemStack rig = installedRig(entity);
      return !rig.isEmpty() && isNetherite(rig) == netherite;
   }

   public static boolean isNetherite(LivingEntity entity) {
      ItemStack rig = installedRig(entity);
      if (rig.isEmpty() && entity != null) {
         rig = entity.getItemBySlot(EquipmentSlot.CHEST);
      }

      return !rig.isEmpty() && BuiltInRegistries.ITEM.getKey(rig.getItem()).getPath().startsWith("netherite");
   }

   public static void toTankSpace(PoseStack poseStack, boolean implanted) {
      poseStack.translate(-0.5F, 0.625F, 1.0F);
      poseStack.scale(1.0F, -1.0F, -1.0F);
      poseStack.translate(0.5F, 0.0F, 0.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.translate(-0.5F, 0.0F, -0.5F);
      if (implanted) {
         poseStack.translate(0.5F, 0.33531252F, 0.125F);
         poseStack.scale(0.9F, 0.9F, 0.9F);
         poseStack.translate(-0.5F, -0.625F, -0.359375F);
      }

      shiftJoints(implanted);
   }

   public static void shiftJoints(boolean implanted) {
      if (!jointsMissing && implanted != jointsShifted) {
         try {
            if (joints == null) {
               Class renderer = Class.forName("dev.tentaworks.client.TentacleRenderer", false, RigImplant.class.getClassLoader());
               Vector3f[][] found = new Vector3f[JOINTS.length][];
               Vector3f[][] originals = new Vector3f[JOINTS.length][];

               for (int j = 0; j < JOINTS.length; j++) {
                  Field field = renderer.getDeclaredField(JOINTS[j]);
                  field.setAccessible(true);
                  found[j] = (Vector3f[])field.get(null);
                  originals[j] = new Vector3f[found[j].length];

                  for (int i = 0; i < found[j].length; i++) {
                     originals[j][i] = new Vector3f(found[j][i]);
                  }
               }

               joints = found;
               originalJoints = originals;
            }
         } catch (RuntimeException | ReflectiveOperationException | LinkageError var7) {
            LOGGER.warn("Failed to reach Octo Arms' tentacle joints", var7);
            jointsMissing = true;
            return;
         }

         for (int j = 0; j < joints.length; j++) {
            for (int i = 0; i < joints[j].length; i++) {
               Vector3f original = originalJoints[j][i];
               joints[j][i].set(original);
               if (implanted) {
                  joints[j][i].add(Math.signum(8.0F - original.x) * 2.2222223F, i >= 2 ? 1.1111112F : 0.0F, 0.0F);
               }
            }
         }

         jointsShifted = implanted;
      }
   }
}
