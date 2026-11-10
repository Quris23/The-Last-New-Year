package lny.octotilt.rig;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public class RigColors {
   private static final Logger LOGGER = LogManager.getLogger("lny_octo_tilt");
   public static final String IMPLANT = "tentacle_rig";
   public static final String IMPLANT_NETHERITE = "tentacle_rig_netherite";
   public static final ResourceLocation MASK = ResourceLocation.fromNamespaceAndPath("lny_octo_tilt", "textures/entity/tentacle_rig_parts.png");
   private static final String[] PARTS = new String[]{"brass", "copper", "steel", "dark", "lights", "wire"};
   private static final int[] PALETTE = new int[]{16766976, 16742912, 9413560, 5913228, 65382, 16711935};
   private static final int BRASS = 0;
   private static final int DARK = 3;
   private static final int LIGHTS = 4;
   private static final int TILE = 32;
   private static final ResourceLocation[] TILES = new ResourceLocation[]{
      ResourceLocation.fromNamespaceAndPath("tentaworks", "tentacle_sleeve"),
      ResourceLocation.fromNamespaceAndPath("tentaworks", "tentacle_rod"),
      ResourceLocation.fromNamespaceAndPath("tentaworks", "tentacle_collar"),
      ResourceLocation.fromNamespaceAndPath("tentaworks", "tentacle_hub"),
      ResourceLocation.fromNamespaceAndPath("tentaworks", "tentacle_cog"),
      ResourceLocation.fromNamespaceAndPath("create", "sticker")
   };
   private static final int HUB_TILE = 3;
   private static final int STICKER_TILE = 5;
   private static final int WIDTH = 32 * TILES.length;
   private static final int MAX_TEXTURES = 32;
   private static final RigColors.Sheet[] SHEETS = new RigColors.Sheet[2];
   private static final Map<String, ResourceLocation> TEXTURES = new LinkedHashMap<>(16, 0.75F, true);
   private static MultiBufferSource buffers;
   private static ResourceLocation texture;
   private static ModelPart previewBody;
   private static Supplier colorsAttachment;
   private static Method colorsMap;
   private static Method drawPosed;
   private static Object[] tiers;
   private static boolean chromewareMissing;

   public static boolean isRigRow(String implant) {
      // A single row for both tiers: the colours of one go to the other when the rig is changed
      return "tentacle_rig".equals(implant);
   }

   public static void begin(MultiBufferSource buffers, Player player) {
      RigColors.buffers = buffers;
      texture = textureFor(colorsOf(player), RigImplant.isNetherite(player));
   }

   public static void end() {
      buffers = null;
      texture = null;
      RigImplant.shiftJoints(false);
   }

   public static boolean quad(
      VertexConsumer atlasConsumer,
      Pose pose,
      TextureAtlasSprite sprite,
      int light,
      float nx,
      float ny,
      float nz,
      float uFrom,
      float uTo,
      float vFrom,
      float vTo,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz
   ) {
      int tile = tile(sprite.contents().name());
      if (tile < 0) {
         return false;
      } else {
         boolean padSide = tile == 5 && vFrom == 0.0F && vTo == 2.0F;
         if (padSide) {
            vFrom = 2.0F;
            vTo = 0.0F;
         }

         float u0;
         float u1;
         float v0;
         float v1;
         VertexConsumer consumer;
         if (texture != null && buffers != null) {
            u0 = ((float)(tile * 32) + uFrom * 32.0F / 16.0F) / (float)WIDTH;
            u1 = ((float)(tile * 32) + uTo * 32.0F / 16.0F) / (float)WIDTH;
            v0 = vFrom / 16.0F;
            v1 = vTo / 16.0F;
            consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
         } else {
            if (!padSide) {
               return false;
            }

            u0 = sprite.getU(uFrom / 16.0F);
            u1 = sprite.getU(uTo / 16.0F);
            v0 = sprite.getV(vFrom / 16.0F);
            v1 = sprite.getV(vTo / 16.0F);
            consumer = atlasConsumer;
         }

         vertex(consumer, pose, ax, ay, az, u0, v0, light, nx, ny, nz);
         vertex(consumer, pose, dx, dy, dz, u0, v1, light, nx, ny, nz);
         vertex(consumer, pose, cx, cy, cz, u1, v1, light, nx, ny, nz);
         vertex(consumer, pose, bx, by, bz, u1, v0, light, nx, ny, nz);
         return true;
      }
   }

   private static void vertex(VertexConsumer consumer, Pose pose, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
      consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
   }

   private static int tile(ResourceLocation sprite) {
      String path = sprite.getPath();
      path = path.substring(path.lastIndexOf(47) + 1).replace("_netherite", "");

      for (int i = 0; i < TILES.length; i++) {
         if (TILES[i].getNamespace().equals(sprite.getNamespace()) && TILES[i].getPath().equals(path)) {
            return i;
         }
      }

      return -1;
   }

   public static List parts() {
      RigColors.Sheet sheet = sheet(false);
      List parts = new ArrayList();
      if (sheet != null) {
         for (int i = 0; i < PARTS.length; i++) {
            if (sheet.count[i] > 0 && i != 4) {
               parts.add(PARTS[i]);
            }
         }
      }

      return parts;
   }

   public static int defaultColor(String part) {
      RigColors.Sheet sheet = sheet(false);
      int group = group(part);
      return sheet != null && group >= 0 && sheet.count[group] > 0 ? sheet.average[group] : 8421504;
   }

   private static int group(String part) {
      for (int i = 0; i < PARTS.length; i++) {
         if (PARTS[i].equals(part)) {
            return i;
         }
      }

      return -1;
   }

   public static void clear() {
      SHEETS[0] = SHEETS[1] = null;

      for (ResourceLocation id : TEXTURES.values()) {
         Minecraft.getInstance().getTextureManager().release(id);
      }

      TEXTURES.clear();
   }

   private static ResourceLocation textureFor(Map colors, boolean netherite) {
      Map chosen = new TreeMap();

      for (int i = 0; colors != null && i < PARTS.length; i++) {
         Integer color = (Integer)colors.get("tentacle_rig:" + PARTS[i]);
         if (color != null && i != 4) {
            chosen.put(i, color);
         }
      }

      String key = (netherite ? "netherite" : "copper") + chosen;
      ResourceLocation cached = TEXTURES.get(key);
      if (cached != null) {
         return cached;
      } else {
         RigColors.Sheet sheet = sheet(netherite);
         if (sheet == null) {
            return null;
         } else {
            NativeImage image = new NativeImage(WIDTH, 32, false);

            for (int ix = 0; ix < sheet.pixels.length; ix++) {
               int argb = sheet.pixels[ix];
               int group = sheet.groups[ix];
               Integer target = group >= 0 ? (Integer)chosen.get(group) : null;
               if (target != null && argb >>> 24 > 0) {
                  float shade = luminance(argb) / Math.max(0.001F, sheet.mean[group]);
                  argb = argb & 0xFF000000
                     | clamp((float)ARGB32.red(target) * shade) << 16
                     | clamp((float)ARGB32.green(target) * shade) << 8
                     | clamp((float)ARGB32.blue(target) * shade);
               }

               image.setPixelRGBA(ix % WIDTH, ix / WIDTH, ABGR32.fromArgb32(argb));
            }

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("lny_octo_tilt", "tentacle_tint/" + Integer.toHexString(key.hashCode()));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
            TEXTURES.put(key, id);
            if (TEXTURES.size() > 32) {
               Iterator eldest = TEXTURES.entrySet().iterator();
               Minecraft.getInstance().getTextureManager().release((ResourceLocation)((Entry)eldest.next()).getValue());
               eldest.remove();
            }

            return id;
         }
      }
   }

   private static RigColors.Sheet sheet(boolean netherite) {
      int row = netherite ? 1 : 0;
      if (SHEETS[row] == null) {
         SHEETS[row] = RigColors.Sheet.read(Minecraft.getInstance().getResourceManager(), netherite);
      }

      return SHEETS[row].pixels == null ? null : SHEETS[row];
   }

   private static boolean isRed(int argb) {
      int r = ARGB32.red(argb);
      return argb >>> 24 >= 128 && r >= 60 && r > ARGB32.green(argb) * 2 && r > ARGB32.blue(argb) * 2;
   }

   private static float luminance(int argb) {
      return (0.2126F * (float)ARGB32.red(argb) + 0.7152F * (float)ARGB32.green(argb) + 0.0722F * (float)ARGB32.blue(argb)) / 255.0F;
   }

   private static int clamp(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   private static Map colorsOf(Player player) {
      if (!chromewareMissing && player != null) {
         try {
            if (colorsAttachment == null) {
               Field field = Class.forName("com.chromeware.cyberware.PlayerData", false, RigColors.class.getClassLoader()).getField("IMPLANT_COLORS");
               colorsAttachment = (Supplier)field.get(null);
            }

            Object colors = player.getData((AttachmentType)colorsAttachment.get());
            return colorsMap(colors);
         } catch (ClassCastException | ReflectiveOperationException | LinkageError var21) {
            chromewareMissing = true;
            return null;
         }
      } else {
         return null;
      }
   }

   private static Map colorsMap(Object implantColors) throws ReflectiveOperationException {
      if (colorsMap == null) {
         colorsMap = implantColors.getClass().getMethod("colors");
      }

      return (Map)colorsMap.invoke(implantColors);
   }

   public static void preview(
      GuiGraphics graphics, Object implantColors, float x, float y, float size, float yaw, float pitch, float seconds, boolean netherite
   ) {
      LocalPlayer viewer = Minecraft.getInstance().player;

      Map colors;
      try {
         colors = colorsMap(implantColors);
      } catch (ReflectiveOperationException var19) {
         colors = null;
      }

      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x, y, 150.0F);
      pose.scale(size, size, -size);
      pose.mulPose(Axis.XP.rotationDegrees(-pitch));
      pose.mulPose(Axis.YP.rotationDegrees(yaw));
      pose.scale(0.6F, 0.6F, 0.6F);
      pose.translate(0.0F, -0.375F, 0.0F);
      Lighting.setupForEntityInInventory();
      BufferSource source = graphics.bufferSource();
      int light = 15728880;
      if (previewBody == null) {
         previewBody = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER).getChild("body");
      }

      ResourceLocation skin = viewer != null ? viewer.getSkin().texture() : DefaultPlayerSkin.getDefaultTexture();
      previewBody.render(pose, source.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
      RigImplant.toTankSpace(pose, true);
      buffers = source;
      texture = textureFor(colors, netherite);

      try {
         drawPosed(pose, source, netherite, Mth.clamp(seconds * 0.6F, 0.0F, 1.0F), seconds * 20.0F, light);
      } finally {
         end();
      }

      graphics.flush();
      Lighting.setupFor3DItems();
      pose.popPose();
   }

   private static void drawPosed(PoseStack pose, MultiBufferSource source, boolean netherite, float deploy, float time, int light) {
      try {
         if (drawPosed == null) {
            ClassLoader loader = RigColors.class.getClassLoader();
            Class tier = Class.forName("dev.tentaworks.rig.RigTier", false, loader);
            tiers = tier.getEnumConstants();
            drawPosed = Class.forName("dev.tentaworks.client.TentacleRenderer", false, loader)
               .getMethod(
                  "drawPosed",
                  PoseStack.class,
                  MultiBufferSource.class,
                  tier,
                  Vector3f[].class,
                  Vector3f[].class,
                  float[].class,
                  float.class,
                  float.class,
                  int.class
               );
         }

         drawPosed.invoke(null, pose, source, tiers[netherite && tiers.length > 1 ? 1 : 0], new Vector3f[4], new Vector3f[4], new float[4], deploy, time, light);
      } catch (ReflectiveOperationException | LinkageError var81) {
         LOGGER.warn("Failed to draw the tentacle rig preview", var81);
      }
   }

   public static ItemStack icon(boolean netherite) {
      ItemStack rig = RigImplant.installedRig(Minecraft.getInstance().player);
      if (!rig.isEmpty() && RigImplant.isNetherite(rig) == netherite) {
         return rig.copyWithCount(1);
      } else {
         ItemLike item = (ItemLike)BuiltInRegistries.ITEM
            .get(ResourceLocation.fromNamespaceAndPath("tentaworks", (netherite ? "netherite" : "copper") + "_tentacle_rig"));
         return new ItemStack(item);
      }
   }

   private static class Sheet {
      int[] pixels;
      int[] groups;
      final float[] mean = new float[RigColors.PARTS.length];
      final int[] average = new int[RigColors.PARTS.length];
      final int[] count = new int[RigColors.PARTS.length];

      static RigColors.Sheet read(ResourceManager resources, boolean netherite) {
         RigColors.Sheet sheet = new RigColors.Sheet();

         try {
            NativeImage mask = image(resources, RigColors.MASK);

            label170: {
               RigColors.Sheet pixels;
               try {
                  if (mask != null && mask.getWidth() == RigColors.WIDTH && mask.getHeight() == 64) {
                     int[] pixelsx = new int[RigColors.WIDTH * 32];
                     int[] groups = new int[RigColors.WIDTH * 32];

                     for (int t = 0; t < RigColors.TILES.length; t++) {
                        String path = RigColors.TILES[t].getPath()
                           + (netherite && RigColors.TILES[t].getNamespace().equals("tentaworks") ? "_netherite" : "");
                        NativeImage tile = image(
                           resources, ResourceLocation.fromNamespaceAndPath(RigColors.TILES[t].getNamespace(), "textures/block/" + path + ".png")
                        );

                        try {
                           int size = tile == null ? 0 : tile.getWidth();

                           for (int py = 0; py < 32; py++) {
                              for (int px = 0; px < 32; px++) {
                                 int i = py * RigColors.WIDTH + t * 32 + px;
                                 pixelsx[i] = size == 0 ? 0 : ABGR32.fromArgb32(tile.getPixelRGBA(px * size / 32, py * size / 32));
                                 groups[i] = group(ABGR32.fromArgb32(mask.getPixelRGBA(t * 32 + px, (netherite ? 32 : 0) + py)));
                                 if (t == 3 && RigColors.isRed(pixelsx[i])) {
                                    groups[i] = 4;
                                 }
                              }
                           }
                        } catch (Throwable var161) {
                           if (tile != null) {
                              try {
                                 tile.close();
                              } catch (Throwable var151) {
                                 var161.addSuppressed(var151);
                              }
                           }

                           throw var161;
                        }

                        if (tile != null) {
                           tile.close();
                        }
                     }

                     sheet.measure(pixelsx, groups);
                     if (sheet.count[4] > 0 && sheet.count[3] > 0) {
                        for (int i = 0; i < pixelsx.length; i++) {
                           if (groups[i] == 4) {
                              pixelsx[i] = pixelsx[i] & 0xFF000000 | sheet.average[3];
                              groups[i] = 3;
                           }
                        }
                     }

                     for (int ix = 0; ix < groups.length; ix++) {
                        if (groups[ix] == 3) {
                           groups[ix] = 0;
                        }
                     }

                     sheet.measure(pixelsx, groups);
                     sheet.pixels = pixelsx;
                     sheet.groups = groups;
                     break label170;
                  }

                  RigColors.LOGGER.warn("Tentacle rig colour mask {} is missing or not {}x{}", RigColors.MASK, RigColors.WIDTH, 64);
                  pixels = sheet;
               } catch (Throwable var171) {
                  if (mask != null) {
                     try {
                        mask.close();
                     } catch (Throwable var141) {
                        var171.addSuppressed(var141);
                     }
                  }

                  throw var171;
               }

               if (mask != null) {
                  mask.close();
               }

               return pixels;
            }

            if (mask != null) {
               mask.close();
            }
         } catch (Exception var18) {
            RigColors.LOGGER.warn("Failed to read the tentacle rig textures for recolouring", var18);
         }

         return sheet;
      }

      private void measure(int[] pixels, int[] groups) {
         Arrays.fill(this.count, 0);
         Arrays.fill(this.mean, 0.0F);
         Arrays.fill(this.average, 0);
         float[][] sums = new float[RigColors.PARTS.length][4];

         for (int i = 0; i < pixels.length; i++) {
            int group = groups[i];
            if (group >= 0 && pixels[i] >>> 24 >= 10) {
               sums[group][0] = sums[group][0] + RigColors.luminance(pixels[i]);
               sums[group][1] = sums[group][1] + (float)ARGB32.red(pixels[i]);
               sums[group][2] = sums[group][2] + (float)ARGB32.green(pixels[i]);
               sums[group][3] = sums[group][3] + (float)ARGB32.blue(pixels[i]);
               this.count[group]++;
            }
         }

         for (int g = 0; g < RigColors.PARTS.length; g++) {
            int n = this.count[g];
            if (n > 0) {
               this.mean[g] = sums[g][0] / (float)n;
               this.average[g] = (int)(sums[g][1] / (float)n) << 16 | (int)(sums[g][2] / (float)n) << 8 | (int)(sums[g][3] / (float)n);
            }
         }
      }

      private static NativeImage image(ResourceManager resources, ResourceLocation id) throws IOException {
         Optional resource = resources.getResource(id);
         if (resource.isEmpty()) {
            return null;
         } else {
            NativeImage var4;
            try (InputStream stream = ((Resource)resource.get()).open()) {
               var4 = NativeImage.read(stream);
            }

            return var4;
         }
      }

      private static int group(int argb) {
         if (argb >>> 24 < 128) {
            return -1;
         } else {
            int best = -1;
            int bestDistance = 6912;

            for (int g = 0; g < RigColors.PALETTE.length; g++) {
               int dr = ARGB32.red(argb) - (RigColors.PALETTE[g] >> 16 & 0xFF);
               int dg = ARGB32.green(argb) - (RigColors.PALETTE[g] >> 8 & 0xFF);
               int db = ARGB32.blue(argb) - (RigColors.PALETTE[g] & 0xFF);
               int distance = dr * dr + dg * dg + db * db;
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = g;
               }
            }

            return best;
         }
      }
   }
}
