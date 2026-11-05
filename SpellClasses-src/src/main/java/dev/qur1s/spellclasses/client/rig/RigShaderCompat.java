package dev.qur1s.spellclasses.client.rig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.neoforged.fml.ModList;
import org.joml.Matrix4f;

public class RigShaderCompat {
   private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("spellclasses-rig");
   private static boolean initialized;
   private static Object irisApi;
   private static Method isShaderPackInUse;
   private static boolean shadowInitialized;
   private static Field shadowActive;
   private static Field shadowModelView;

   public static boolean areShadersRunning() {
      if (!initialized) {
         initialized = true;
         if (ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus")) {
            try {
               Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
               irisApi = apiClass.getMethod("getInstance").invoke(null);
               isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            } catch (LinkageError | ReflectiveOperationException var1) {
               LOGGER.warn("Failed to hook into the Iris API, shader pack detection will not work", var1);
               isShaderPackInUse = null;
            }
         }
      }

      if (isShaderPackInUse != null) {
         try {
            return (Boolean)isShaderPackInUse.invoke(irisApi);
         } catch (ReflectiveOperationException var2) {
            LOGGER.warn("Failed to query Iris for an active shader pack", var2);
            isShaderPackInUse = null;
         }
      }

      return false;
   }

   public static Matrix4f shadowPassView() {
      if (!shadowInitialized) {
         shadowInitialized = true;
         if (ModList.get().isLoaded("iris")) {
            try {
               Class<?> shadowRenderer = Class.forName("net.irisshaders.iris.shadows.ShadowRenderer");
               shadowActive = shadowRenderer.getField("ACTIVE");
               shadowModelView = shadowRenderer.getField("MODELVIEW");
            } catch (LinkageError | ReflectiveOperationException var2) {
               LOGGER.warn("Failed to hook into Iris's shadow pass", var2);
               shadowActive = null;
            }
         }
      }

      if (shadowActive != null) {
         try {
            if (shadowActive.getBoolean(null)) {
               return (Matrix4f)shadowModelView.get(null);
            }
         } catch (ReflectiveOperationException var1) {
            shadowActive = null;
         }
      }

      return null;
   }
}
