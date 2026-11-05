package dev.qur1s.spellclasses.client.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class TentacleRigImplant {
   private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("spellclasses-rig");
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

   public static ItemStack installedRig(LivingEntity entity) {
      if (!missing && entity != null) {
         try {
            if (installedRig == null) {
               installedRig = Class.forName(
                     "dev.qur1s.spellclasses.OctoRigBackSlot", false, dev.qur1s.spellclasses.client.rig.TentacleRigImplant.class.getClassLoader()
                  )
                  .getMethod("installedRig", LivingEntity.class);
            }

            return (ItemStack)installedRig.invoke(null, entity);
         } catch (LinkageError | ReflectiveOperationException var2) {
            missing = true;
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static boolean isImplanted(LivingEntity entity) {
      return !installedRig(entity).isEmpty();
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
               Class renderer = Class.forName(
                  "dev.tentaworks.client.TentacleRenderer", false, dev.qur1s.spellclasses.client.rig.TentacleRigImplant.class.getClassLoader()
               );
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
         } catch (LinkageError | RuntimeException | ReflectiveOperationException var7) {
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
