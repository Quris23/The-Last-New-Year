package lny.octotilt.gun.client;

import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.GunDisplayInstance;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lny.octotilt.gun.GunImplants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Chromeware's implant colours for the gun of an implant, kept by Chromeware under {@code crane_implant:<part>}.
 * <p>
 * Which pixels of the gun's texture belong to which part is painted in a mask of the same size ({@link #MASK}), each pixel
 * in one of {@link #PALETTE}'s colours: the receiver, the handguard, the barrel with the bolt and the cog, the sights, the
 * charging handle, the lights, the implant's hull and its fittings. With colours chosen, a recoloured copy of the texture
 * (the part's colour, shaded by each pixel's brightness against the part's mean, as Chromeware does it) takes the place of
 * the gun's texture in the texture manager, so TaCZ draws the gun with it everywhere. One texture serves every player's
 * gun: it follows the colours of the player at this client.
 */
public final class GunImplantColors {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation MASK = ResourceLocation.fromNamespaceAndPath("lny_gun_implants", "textures/gun/uv/crane_parts.png");
   private static final ResourceLocation DISPLAY = ResourceLocation.fromNamespaceAndPath("lny_gun_implants", "crane_display");
   // mask colours of GunImplants.PARTS, in that order
   private static final int[] PALETTE = {0xFFD800, 0xA0522D, 0x8FA3B8, 0x5A3A8C, 0xFF7A00, 0x00FF66, 0xFF00FF, 0x00B7FF};
   private static Sheet sheet;
   // the colours the texture in the texture manager was made for ("" = the texture's own file)
   private static String applied = "";
   private static ImplantColors previewColors;
   private static long previewUntil;
   private static ItemStack icon;
   private static ItemStack previewGun;
   /** Whether the parts last asked for by the customize page are this implant's: its rows are packed closer then */
   public static boolean compactRows;

   private GunImplantColors() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener)manager -> clear()));
      NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> tick());
   }

   private static void tick() {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }
      if (previewColors != null && System.currentTimeMillis() < previewUntil) {
         return;
      }
      previewColors = null;
      apply(player.getData(PlayerData.IMPLANT_COLORS));
   }

   private static String signature(ImplantColors colors) {
      StringBuilder key = new StringBuilder();
      for (String part : GunImplants.PARTS) {
         Integer color = colors.get(GunImplants.CRANE, part);
         if (color != null) {
            key.append(part).append('=').append(Integer.toHexString(color)).append(';');
         }
      }
      return key.toString();
   }

   private static void apply(ImplantColors colors) {
      String key = signature(colors);
      if (key.equals(applied)) {
         return;
      }
      ResourceLocation target = target();
      if (target == null) {
         return;
      }
      Minecraft minecraft = Minecraft.getInstance();
      if (key.isEmpty()) {
         // back to the file: the texture manager loads it again when the gun is next drawn
         minecraft.getTextureManager().release(target);
         applied = key;
         return;
      }
      Sheet sheet = sheet(target);
      if (sheet == null) {
         return;
      }
      NativeImage image = new NativeImage(sheet.width, sheet.height, false);
      for (int i = 0; i < sheet.pixels.length; i++) {
         int argb = sheet.pixels[i];
         int group = sheet.groups[i];
         Integer chosen = group >= 0 ? colors.get(GunImplants.CRANE, GunImplants.PARTS.get(group)) : null;
         if (chosen != null && argb >>> 24 > 0) {
            float shade = luminance(argb) / Math.max(0.001F, sheet.mean[group]);
            argb = argb & 0xFF000000 | clamp(FastColor.ARGB32.red(chosen) * shade) << 16 | clamp(FastColor.ARGB32.green(chosen) * shade) << 8 | clamp(FastColor.ARGB32.blue(chosen) * shade);
         }
         image.setPixelRGBA(i % sheet.width, i / sheet.width, FastColor.ABGR32.fromArgb32(argb));
      }
      minecraft.getTextureManager().register(target, new DynamicTexture(image));
      applied = key;
   }

   // the texture TaCZ draws the gun with
   private static ResourceLocation target() {
      try {
         return TimelessAPI.getGunDisplay(DISPLAY, GunImplants.CRANE_GUN).map(GunDisplayInstance::getModelTexture).orElse(null);
      } catch (RuntimeException e) {
         return null;
      }
   }

   /** The parts painted in the mask, in the order shown on Chromeware's customize page */
   public static List<String> parts() {
      ResourceLocation target = target();
      Sheet sheet = target == null ? null : sheet(target);
      List<String> parts = new ArrayList<>();
      if (sheet != null) {
         for (int i = 0; i < GunImplants.PARTS.size(); i++) {
            if (sheet.count[i] > 0) {
               parts.add(GunImplants.PARTS.get(i));
            }
         }
      }
      return parts;
   }

   /** The part's own colour (the average of its pixels) */
   public static int defaultColor(String part) {
      ResourceLocation target = target();
      Sheet sheet = target == null ? null : sheet(target);
      int group = GunImplants.PARTS.indexOf(part);
      return sheet != null && group >= 0 && sheet.count[group] > 0 ? sheet.average[group] : 0x808080;
   }

   public static boolean installed(Player player) {
      return player != null && ArmImplants.has(player, GunImplants::isImplant);
   }

   /** The item standing for the implant in Chromeware's implant list */
   public static ItemStack icon() {
      if (icon == null) {
         icon = new ItemStack(GunImplants.CRANE_IMPLANT.get());
      }
      return icon;
   }

   /** The gun turning before the camera on Chromeware's customize page, in the colours being chosen there */
   public static void preview(GuiGraphics graphics, Object pageColors, float x, float y, float size, float yaw, float pitch) {
      ImplantColors colors = (ImplantColors)pageColors;
      previewColors = colors;
      previewUntil = System.currentTimeMillis() + 400L;
      apply(colors);
      Minecraft minecraft = Minecraft.getInstance();
      if (previewGun == null && minecraft.level != null) {
         previewGun = com.tacz.guns.api.item.builder.GunItemBuilder.create().setId(GunImplants.CRANE_GUN).forceBuild(minecraft.level.registryAccess());
      }
      if (previewGun == null || previewGun.isEmpty()) {
         return;
      }
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x, y, 150.0F);
      pose.scale(size, size, -size);
      pose.mulPose(Axis.XP.rotationDegrees(-pitch));
      pose.mulPose(Axis.YP.rotationDegrees(yaw));
      pose.scale(0.8F, -0.8F, 0.8F);
      Lighting.setupForEntityInInventory();
      MultiBufferSource.BufferSource buffers = graphics.bufferSource();
      minecraft.getItemRenderer().renderStatic(previewGun, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffers, minecraft.level, 0);
      graphics.flush();
      Lighting.setupFor3DItems();
      pose.popPose();
   }

   public static void clear() {
      sheet = null;
      previewGun = null;
      if (!applied.isEmpty()) {
         ResourceLocation target = target();
         if (target != null) {
            Minecraft.getInstance().getTextureManager().release(target);
         }
      }
      applied = "";
   }

   private static Sheet sheet(ResourceLocation texture) {
      if (sheet == null) {
         sheet = Sheet.read(Minecraft.getInstance().getResourceManager(), texture);
      }
      return sheet.pixels == null ? null : sheet;
   }

   private static float luminance(int argb) {
      return (0.2126F * FastColor.ARGB32.red(argb) + 0.7152F * FastColor.ARGB32.green(argb) + 0.0722F * FastColor.ARGB32.blue(argb)) / 255.0F;
   }

   private static int clamp(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   /** The gun's texture, with the part of each pixel read from the mask */
   private static class Sheet {
      int width;
      int height;
      int[] pixels;
      int[] groups;
      final float[] mean = new float[PALETTE.length];
      final int[] average = new int[PALETTE.length];
      final int[] count = new int[PALETTE.length];

      static Sheet read(ResourceManager resources, ResourceLocation textureId) {
         Sheet sheet = new Sheet();
         try (NativeImage texture = image(resources, textureId); NativeImage mask = image(resources, MASK)) {
            if (texture == null || mask == null || mask.getWidth() != texture.getWidth() || mask.getHeight() != texture.getHeight()) {
               LOGGER.warn("Gun implant colour mask {} is missing or not the size of {}", MASK, textureId);
               return sheet;
            }
            int width = texture.getWidth();
            int height = texture.getHeight();
            int[] pixels = new int[width * height];
            int[] groups = new int[width * height];
            float[][] sums = new float[PALETTE.length][4];
            for (int y = 0; y < height; y++) {
               for (int x = 0; x < width; x++) {
                  int i = y * width + x;
                  pixels[i] = FastColor.ABGR32.fromArgb32(texture.getPixelRGBA(x, y));
                  groups[i] = group(FastColor.ABGR32.fromArgb32(mask.getPixelRGBA(x, y)));
                  int group = groups[i];
                  if (group >= 0 && pixels[i] >>> 24 >= 10) {
                     sums[group][0] += luminance(pixels[i]);
                     sums[group][1] += FastColor.ARGB32.red(pixels[i]);
                     sums[group][2] += FastColor.ARGB32.green(pixels[i]);
                     sums[group][3] += FastColor.ARGB32.blue(pixels[i]);
                     sheet.count[group]++;
                  }
               }
            }
            for (int g = 0; g < PALETTE.length; g++) {
               int n = sheet.count[g];
               if (n > 0) {
                  sheet.mean[g] = sums[g][0] / n;
                  sheet.average[g] = (int)(sums[g][1] / n) << 16 | (int)(sums[g][2] / n) << 8 | (int)(sums[g][3] / n);
               }
            }
            sheet.width = width;
            sheet.height = height;
            sheet.pixels = pixels;
            sheet.groups = groups;
         } catch (Exception e) {
            LOGGER.warn("Failed to read the gun implant texture for recolouring", e);
         }
         return sheet;
      }

      private static NativeImage image(ResourceManager resources, ResourceLocation id) throws java.io.IOException {
         Optional<Resource> resource = resources.getResource(id);
         if (resource.isEmpty()) {
            return null;
         }
         try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
         }
      }

      // The palette colour nearest to the mask pixel, if near enough
      private static int group(int argb) {
         if (argb >>> 24 < 128) {
            return -1;
         }
         int best = -1;
         int bestDistance = 40 * 40 * 3;
         for (int g = 0; g < PALETTE.length; g++) {
            int dr = FastColor.ARGB32.red(argb) - (PALETTE[g] >> 16 & 0xFF);
            int dg = FastColor.ARGB32.green(argb) - (PALETTE[g] >> 8 & 0xFF);
            int db = FastColor.ARGB32.blue(argb) - (PALETTE[g] & 0xFF);
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
