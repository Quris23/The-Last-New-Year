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
import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Chromeware's implant colours for the guns of the gun implants, kept by Chromeware under {@code <implant>:<part>}.
 * <p>
 * Which pixels of a gun's texture belong to which part is painted in a mask of the same size (the kind's mask), each
 * pixel in one of {@link #PALETTE}'s colours: the receiver, the handguard, the barrel with the bolt, the sights, the
 * handle, the lights, the implant's hull and its fittings. With colours chosen, a recoloured copy of the texture (the
 * part's colour, shaded by each pixel's brightness against the part's mean, as Chromeware does it) takes the place of
 * the gun's texture in the texture manager, so TaCZ draws the gun with it everywhere. One texture serves every player's
 * gun: it follows the colours of the player at this client.
 * <p>
 * Attachments fitted to an implant's gun are drawn by TaCZ from their own textures, which have no mask. They get a
 * recoloured copy too ({@link #attachment}), their pixels sorted into the same parts by colour: brass tones follow the
 * receiver, browns the handguard, light greys the barrel, dark greys the sights, copper the handle, green the lights.
 */
public final class GunImplantColors {
   private static final Logger LOGGER = LogUtils.getLogger();
   // mask colours of GunImplants.PARTS, in that order
   private static final int[] PALETTE = {0xFFD800, 0xA0522D, 0x8FA3B8, 0x5A3A8C, 0xFF7A00, 0x00FF66, 0xFF00FF, 0x00B7FF};
   private static final Map<String, State> STATES = new HashMap<>();
   // recoloured attachment textures: "<implant>|<colours>|<texture>" -> texture id (or the original when it failed)
   private static final Map<String, ResourceLocation> ATTACHMENTS = new HashMap<>();
   private static ImplantColors previewColors;
   private static long previewUntil;
   /** Whether the parts last asked for by the customize page are a gun implant's: its rows are packed closer then */
   public static boolean compactRows;
   /** The implant whose gun TaCZ is drawing right now (set around the gun's render by the mixin), or null */
   public static GunImplants.Kind drawing;

   private static final class State {
      Sheet sheet;
      // the colours the texture in the texture manager was made for ("" = the texture's own file)
      String applied = "";
      ItemStack icon;
      ItemStack previewGun;
   }

   private GunImplantColors() {
   }

   public static void register(IEventBus modBus) {
      modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener)manager -> clear()));
      NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> tick());
   }

   private static State state(GunImplants.Kind kind) {
      return STATES.computeIfAbsent(kind.key(), k -> new State());
   }

   private static ImplantColors current() {
      Player player = Minecraft.getInstance().player;
      if (previewColors != null && System.currentTimeMillis() < previewUntil) {
         return previewColors;
      }
      previewColors = null;
      return player == null ? ImplantColors.EMPTY : player.getData(PlayerData.IMPLANT_COLORS);
   }

   private static void tick() {
      if (Minecraft.getInstance().player == null) {
         return;
      }
      ImplantColors colors = current();
      for (GunImplants.Kind kind : GunImplants.kinds()) {
         apply(kind, colors);
      }
   }

   private static String signature(GunImplants.Kind kind, ImplantColors colors) {
      StringBuilder key = new StringBuilder();
      for (String part : GunImplants.PARTS) {
         Integer color = colors.get(kind.key(), part);
         if (color != null) {
            key.append(part).append('=').append(Integer.toHexString(color)).append(';');
         }
      }
      return key.toString();
   }

   private static void apply(GunImplants.Kind kind, ImplantColors colors) {
      State state = state(kind);
      String key = signature(kind, colors);
      if (key.equals(state.applied)) {
         return;
      }
      ResourceLocation target = target(kind);
      if (target == null) {
         return;
      }
      Minecraft minecraft = Minecraft.getInstance();
      if (key.isEmpty()) {
         // back to the file: the texture manager loads it again when the gun is next drawn
         minecraft.getTextureManager().release(target);
         state.applied = key;
         return;
      }
      Sheet sheet = sheet(kind, target);
      if (sheet == null) {
         return;
      }
      NativeImage image = new NativeImage(sheet.width, sheet.height, false);
      for (int i = 0; i < sheet.pixels.length; i++) {
         int argb = sheet.pixels[i];
         int group = sheet.groups[i];
         Integer chosen = group >= 0 ? colors.get(kind.key(), GunImplants.PARTS.get(group)) : null;
         if (chosen != null && argb >>> 24 > 0) {
            argb = shade(argb, chosen, sheet.mean[group]);
         }
         image.setPixelRGBA(i % sheet.width, i / sheet.width, FastColor.ABGR32.fromArgb32(argb));
      }
      minecraft.getTextureManager().register(target, new DynamicTexture(image));
      state.applied = key;
   }

   private static int shade(int argb, int chosen, float mean) {
      float shade = luminance(argb) / Math.max(0.001F, mean);
      return argb & 0xFF000000 | clamp(FastColor.ARGB32.red(chosen) * shade) << 16 | clamp(FastColor.ARGB32.green(chosen) * shade) << 8 | clamp(FastColor.ARGB32.blue(chosen) * shade);
   }

   // the texture TaCZ draws the gun with
   private static ResourceLocation target(GunImplants.Kind kind) {
      try {
         return TimelessAPI.getGunDisplay(kind.display(), kind.gun()).map(GunDisplayInstance::getModelTexture).orElse(null);
      } catch (RuntimeException e) {
         return null;
      }
   }

   /**
    * The texture to draw an attachment of the implant gun being drawn with: a copy in the gun's chosen colours, or the
    * texture itself when no colours are chosen (or no implant gun is being drawn).
    */
   public static ResourceLocation attachment(ResourceLocation texture) {
      GunImplants.Kind kind = drawing;
      if (kind == null || texture == null) {
         return texture;
      }
      ImplantColors colors = current();
      String signature = signature(kind, colors);
      if (signature.isEmpty()) {
         return texture;
      }
      String key = kind.key() + "|" + signature + "|" + texture;
      ResourceLocation done = ATTACHMENTS.get(key);
      if (done != null) {
         return done;
      }
      ResourceLocation made = texture;
      Sheet gun = state(kind).sheet;
      try (NativeImage source = Sheet.image(Minecraft.getInstance().getResourceManager(), texture)) {
         if (source != null && gun != null && gun.pixels != null) {
            int width = source.getWidth();
            int height = source.getHeight();
            int[] pixels = new int[width * height];
            int[] groups = new int[width * height];
            float[] sum = new float[PALETTE.length];
            int[] count = new int[PALETTE.length];
            for (int i = 0; i < pixels.length; i++) {
               pixels[i] = FastColor.ABGR32.fromArgb32(source.getPixelRGBA(i % width, i / width));
               groups[i] = pixels[i] >>> 24 < 10 ? -1 : classify(pixels[i]);
               if (groups[i] >= 0) {
                  sum[groups[i]] += luminance(pixels[i]);
                  count[groups[i]]++;
               }
            }
            NativeImage image = new NativeImage(width, height, false);
            for (int i = 0; i < pixels.length; i++) {
               int argb = pixels[i];
               int group = groups[i];
               Integer chosen = group >= 0 ? colors.get(kind.key(), GunImplants.PARTS.get(group)) : null;
               if (chosen != null) {
                  argb = shade(argb, chosen, sum[group] / count[group]);
               }
               image.setPixelRGBA(i % width, i / width, FastColor.ABGR32.fromArgb32(argb));
            }
            made = ResourceLocation.fromNamespaceAndPath("lny_octo_tilt", "gun_tint/" + Integer.toHexString(key.hashCode()));
            Minecraft.getInstance().getTextureManager().register(made, new DynamicTexture(image));
         }
      } catch (Exception e) {
         LOGGER.warn("Failed to recolour the attachment texture {}", texture, e);
      }
      if (ATTACHMENTS.size() > 96) {
         clearAttachments();
      }
      ATTACHMENTS.put(key, made);
      return made;
   }

   // The part a pixel of an unmasked texture goes with, by its colour (the same rule the masks were painted by)
   private static int classify(int argb) {
      float[] hsv = Color.RGBtoHSB(FastColor.ARGB32.red(argb), FastColor.ARGB32.green(argb), FastColor.ARGB32.blue(argb), null);
      float hue = hsv[0] * 360.0F;
      float value = hsv[2] * 255.0F;
      if (hsv[1] < 0.15F) {
         if (value > 235.0F && hue > 20.0F && hue < 60.0F) {
            return 0;
         }
         return value >= 86.0F ? 2 : 3;
      }
      if (hue >= 70.0F && hue <= 170.0F) {
         return 5;
      }
      if (hue >= 27.0F && hue < 70.0F) {
         return value >= 120.0F ? 0 : 1;
      }
      if (hue > 170.0F && hue < 330.0F) {
         return -1; // blues and purples are no part of a gun: left as they are
      }
      return value >= 135.0F ? 4 : 1;
   }

   /** The parts painted in the mask, in the order shown on Chromeware's customize page */
   public static List<String> parts(String implant) {
      GunImplants.Kind kind = GunImplants.byKey(implant);
      ResourceLocation target = kind == null ? null : target(kind);
      Sheet sheet = target == null ? null : sheet(kind, target);
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
   public static int defaultColor(String implant, String part) {
      GunImplants.Kind kind = GunImplants.byKey(implant);
      ResourceLocation target = kind == null ? null : target(kind);
      Sheet sheet = target == null ? null : sheet(kind, target);
      int group = GunImplants.PARTS.indexOf(part);
      return sheet != null && group >= 0 && sheet.count[group] > 0 ? sheet.average[group] : 0x808080;
   }

   public static boolean installed(Player player, String implant) {
      GunImplants.Kind kind = GunImplants.byKey(implant);
      return player != null && kind != null && ArmImplants.has(player, stack -> stack.is(kind.item().get()));
   }

   /** The item standing for the implant in Chromeware's implant list */
   public static ItemStack icon(String implant) {
      GunImplants.Kind kind = GunImplants.byKey(implant);
      if (kind == null) {
         return ItemStack.EMPTY;
      }
      State state = state(kind);
      if (state.icon == null) {
         state.icon = new ItemStack(kind.item().get());
      }
      return state.icon;
   }

   /** The gun turning before the camera on Chromeware's customize page, in the colours being chosen there */
   public static void preview(GuiGraphics graphics, String implant, Object pageColors, float x, float y, float size, float yaw, float pitch) {
      GunImplants.Kind kind = GunImplants.byKey(implant);
      if (kind == null) {
         return;
      }
      ImplantColors colors = (ImplantColors)pageColors;
      previewColors = colors;
      previewUntil = System.currentTimeMillis() + 400L;
      apply(kind, colors);
      State state = state(kind);
      Minecraft minecraft = Minecraft.getInstance();
      if (state.previewGun == null && minecraft.level != null) {
         state.previewGun = com.tacz.guns.api.item.builder.GunItemBuilder.create().setId(kind.gun()).forceBuild(minecraft.level.registryAccess());
      }
      if (state.previewGun == null || state.previewGun.isEmpty()) {
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
      minecraft.getItemRenderer().renderStatic(state.previewGun, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffers, minecraft.level, 0);
      graphics.flush();
      Lighting.setupFor3DItems();
      pose.popPose();
   }

   public static void clear() {
      for (GunImplants.Kind kind : GunImplants.kinds()) {
         State state = state(kind);
         if (!state.applied.isEmpty()) {
            ResourceLocation target = target(kind);
            if (target != null) {
               Minecraft.getInstance().getTextureManager().release(target);
            }
         }
      }
      STATES.clear();
      clearAttachments();
   }

   private static void clearAttachments() {
      for (Map.Entry<String, ResourceLocation> entry : ATTACHMENTS.entrySet()) {
         if (entry.getValue().getNamespace().equals("lny_octo_tilt")) {
            Minecraft.getInstance().getTextureManager().release(entry.getValue());
         }
      }
      ATTACHMENTS.clear();
   }

   private static Sheet sheet(GunImplants.Kind kind, ResourceLocation texture) {
      State state = state(kind);
      if (state.sheet == null) {
         state.sheet = Sheet.read(Minecraft.getInstance().getResourceManager(), texture, kind.mask());
      }
      return state.sheet.pixels == null ? null : state.sheet;
   }

   private static float luminance(int argb) {
      return (0.2126F * FastColor.ARGB32.red(argb) + 0.7152F * FastColor.ARGB32.green(argb) + 0.0722F * FastColor.ARGB32.blue(argb)) / 255.0F;
   }

   private static int clamp(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   /** A gun's texture, with the part of each pixel read from the mask */
   private static class Sheet {
      int width;
      int height;
      int[] pixels;
      int[] groups;
      final float[] mean = new float[PALETTE.length];
      final int[] average = new int[PALETTE.length];
      final int[] count = new int[PALETTE.length];

      static Sheet read(ResourceManager resources, ResourceLocation textureId, ResourceLocation maskId) {
         Sheet sheet = new Sheet();
         try (NativeImage texture = image(resources, textureId); NativeImage mask = image(resources, maskId)) {
            if (texture == null || mask == null || mask.getWidth() != texture.getWidth() || mask.getHeight() != texture.getHeight()) {
               LOGGER.warn("Gun implant colour mask {} is missing or not the size of {}", maskId, textureId);
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

      static NativeImage image(ResourceManager resources, ResourceLocation id) throws java.io.IOException {
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
