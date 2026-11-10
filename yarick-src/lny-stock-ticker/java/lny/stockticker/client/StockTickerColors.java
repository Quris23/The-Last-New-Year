package lny.stockticker.client;

import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import lny.stockticker.StockTickerMod;
import lny.stockticker.StockTickerImplant;

/**
 * Chromeware's implant colours for the portable stock ticker sliding out of the wrist (StockTickerArmRenderer), kept by
 * Chromeware with the other implants under {@code stock_ticker:<part>}.
 * <p>
 * Which pixels of the ticker's texture ({@link #TEXTURE}) belong to which part is painted in a mask of the same size,
 * {@link #MASK}, each pixel in one of {@link #PALETTE}'s colours (any other colour, or transparent, is left as it is): the
 * casing in brass's, the screen in lights', the metal in steel's, the red inlays in copper's and the antenna's tip in wire's.
 * A recoloured copy is made per set of colours, the way Chromeware recolours its own implants (the part's colour, shaded by
 * each pixel's brightness against the part's mean), and the ticker's model is drawn from it instead of the block atlas.
 * Only used with Chromeware installed.
 */
public final class StockTickerColors {
   public static final String IMPLANT = "stock_ticker";
   public static final ResourceLocation TEXTURE = StockTickerMod.id("textures/item/stock_ticker_arm.png");
   public static final ResourceLocation MASK = StockTickerMod.id("textures/item/stock_ticker_arm_parts.png");
   // Chromeware's part names, with the mask colour of each
   private static final String[] PARTS = {"brass", "copper", "steel", "dark", "lights", "wire"};
   private static final int[] PALETTE = {0xFFD800, 0xFF7A00, 0x8FA3B8, 0x5A3A8C, 0x00FF66, 0xFF00FF};
   // The order on Chromeware's customize page: casing, screen, metal, inlays, antenna
   private static final String[] SHOWN = {"brass", "lights", "steel", "copper", "wire", "dark"};
   private static final int MAX_TEXTURES = 32;
   private static final Map<String, ResourceLocation> TEXTURES = new LinkedHashMap<>(16, 0.75F, true);
   private static final RandomSource RANDOM = RandomSource.create();
   private static Sheet sheet;
   private static ItemStack icon;

   private StockTickerColors() {
   }

   /** The ticker's texture with the player's colours */
   public static ResourceLocation texture(Player player) {
      return texture(player == null ? ImplantColors.EMPTY : player.getData(PlayerData.IMPLANT_COLORS));
   }

   public static ResourceLocation texture(ImplantColors colors) {
      Map<Integer, Integer> chosen = new TreeMap<>();
      for (int i = 0; i < PARTS.length; i++) {
         Integer color = colors.get(IMPLANT, PARTS[i]);
         if (color != null) {
            chosen.put(i, color);
         }
      }
      if (chosen.isEmpty()) {
         return TEXTURE;
      }
      String key = chosen.toString();
      ResourceLocation cached = TEXTURES.get(key);
      if (cached != null) {
         return cached;
      }
      Sheet sheet = sheet();
      if (sheet == null) {
         return TEXTURE;
      }
      NativeImage image = new NativeImage(sheet.width, sheet.height, false);
      for (int i = 0; i < sheet.pixels.length; i++) {
         int argb = sheet.pixels[i];
         int group = sheet.groups[i];
         Integer target = group >= 0 ? chosen.get(group) : null;
         if (target != null && argb >>> 24 > 0) {
            float shade = luminance(argb) / Math.max(0.001F, sheet.mean[group]);
            argb = argb & 0xFF000000 | clamp(FastColor.ARGB32.red(target) * shade) << 16 | clamp(FastColor.ARGB32.green(target) * shade) << 8 | clamp(FastColor.ARGB32.blue(target) * shade);
         }
         image.setPixelRGBA(i % sheet.width, i / sheet.width, FastColor.ABGR32.fromArgb32(argb));
      }
      ResourceLocation id = StockTickerMod.id("stock_ticker_tint/" + Integer.toHexString(key.hashCode()));
      Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
      TEXTURES.put(key, id);
      if (TEXTURES.size() > MAX_TEXTURES) {
         Iterator<Map.Entry<String, ResourceLocation>> eldest = TEXTURES.entrySet().iterator();
         Minecraft.getInstance().getTextureManager().release(eldest.next().getValue());
         eldest.remove();
      }
      return id;
   }

   /**
    * Draws the ticker's model (its quads, as the item renderer would with no display transform) from the given texture
    * instead of the block atlas.
    */
   public static void draw(BakedModel model, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, int light) {
      VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
      stack.pushPose();
      stack.translate(-0.5F, -0.5F, -0.5F);
      PoseStack.Pose pose = stack.last();
      for (Direction side : Direction.values()) {
         for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
            quad(consumer, pose, quad, light);
         }
      }
      for (BakedQuad quad : model.getQuads(null, null, RANDOM)) {
         quad(consumer, pose, quad, light);
      }
      stack.popPose();
   }

   private static void quad(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, int light) {
      int[] data = quad.getVertices();
      int stride = data.length / 4;
      TextureAtlasSprite sprite = quad.getSprite();
      float du = sprite.getU1() - sprite.getU0();
      float dv = sprite.getV1() - sprite.getV0();
      for (int v = 0; v < 4; v++) {
         int i = v * stride;
         float u = (Float.intBitsToFloat(data[i + 4]) - sprite.getU0()) / du;
         float w = (Float.intBitsToFloat(data[i + 5]) - sprite.getV0()) / dv;
         // the normal packed with the vertex, else the face's
         int packed = data[i + 7];
         float nx = (byte)(packed & 0xFF) / 127.0F;
         float ny = (byte)(packed >> 8 & 0xFF) / 127.0F;
         float nz = (byte)(packed >> 16 & 0xFF) / 127.0F;
         if (nx == 0.0F && ny == 0.0F && nz == 0.0F) {
            nx = quad.getDirection().getStepX();
            ny = quad.getDirection().getStepY();
            nz = quad.getDirection().getStepZ();
         }
         consumer.addVertex(pose, Float.intBitsToFloat(data[i]), Float.intBitsToFloat(data[i + 1]), Float.intBitsToFloat(data[i + 2]))
            .setColor(-1)
            .setUv(u, w)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
      }
   }

   /** The parts painted in the mask, in the order shown on Chromeware's customize page */
   public static List<String> parts() {
      Sheet sheet = sheet();
      List<String> parts = new ArrayList<>();
      if (sheet != null) {
         for (String part : SHOWN) {
            int group = group(part);
            if (sheet.count[group] > 0) {
               parts.add(part);
            }
         }
      }
      return parts;
   }

   /** The part's own colour (the average of its pixels) */
   public static int defaultColor(String part) {
      Sheet sheet = sheet();
      int group = group(part);
      return sheet != null && group >= 0 && sheet.count[group] > 0 ? sheet.average[group] : 0x808080;
   }

   public static boolean valid(String part) {
      return group(part) >= 0;
   }

   private static int group(String part) {
      for (int i = 0; i < PARTS.length; i++) {
         if (PARTS[i].equals(part)) {
            return i;
         }
      }
      return -1;
   }

   public static boolean installed(Player player) {
      return player != null && !StockTickerImplant.find(player).isEmpty();
   }

   /** The item standing for the ticker in Chromeware's implant list */
   public static ItemStack icon() {
      if (icon == null) {
         icon = new ItemStack(BuiltInRegistries.ITEM.get(StockTickerImplant.TICKER));
      }
      return icon;
   }

   /**
    * The ticker turning before the camera on Chromeware's customize page (in place of its ImplantPreview.render), its screen to
    * the front and the antenna up.
    */
   public static void preview(GuiGraphics graphics, ImplantColors colors, float x, float y, float size, float yaw, float pitch) {
      BakedModel model = StockTickerArmRenderer.model();
      if (model == null) {
         return;
      }
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(x, y, 150.0F);
      pose.scale(size, size, -size);
      pose.mulPose(Axis.XP.rotationDegrees(-pitch));
      pose.mulPose(Axis.YP.rotationDegrees(yaw));
      // the body (10 x 14 px) and the antenna (7 px past it) about as tall as the arms of the other previews
      pose.scale(0.7F, -0.7F, 0.7F);
      pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
      pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      // the middle of the body and antenna (8, 1, 4 px) onto the origin, the model then drawn shifted by half a block
      pose.translate(0.0F, 0.5F - 1.0F / 16.0F, 0.25F);
      Lighting.setupForEntityInInventory();
      MultiBufferSource.BufferSource buffers = graphics.bufferSource();
      draw(model, pose, buffers, texture(colors), 0xF000F0);
      graphics.flush();
      Lighting.setupFor3DItems();
      pose.popPose();
   }

   public static void clear() {
      sheet = null;
      for (ResourceLocation id : TEXTURES.values()) {
         Minecraft.getInstance().getTextureManager().release(id);
      }
      TEXTURES.clear();
   }

   private static Sheet sheet() {
      if (sheet == null) {
         sheet = Sheet.read(Minecraft.getInstance().getResourceManager());
      }
      return sheet.pixels == null ? null : sheet;
   }

   private static float luminance(int argb) {
      return (0.2126F * FastColor.ARGB32.red(argb) + 0.7152F * FastColor.ARGB32.green(argb) + 0.0722F * FastColor.ARGB32.blue(argb)) / 255.0F;
   }

   private static int clamp(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   /** The ticker's texture, with the part of each pixel read from the mask */
   private static class Sheet {
      int width;
      int height;
      int[] pixels;
      int[] groups;
      final float[] mean = new float[PARTS.length];
      final int[] average = new int[PARTS.length];
      final int[] count = new int[PARTS.length];

      static Sheet read(ResourceManager resources) {
         Sheet sheet = new Sheet();
         try (NativeImage texture = image(resources, TEXTURE); NativeImage mask = image(resources, MASK)) {
            if (texture == null || mask == null || mask.getWidth() != texture.getWidth() || mask.getHeight() != texture.getHeight()) {
               StockTickerMod.LOGGER.warn("Stock ticker colour mask {} is missing or not the size of {}", MASK, TEXTURE);
               return sheet;
            }
            int width = texture.getWidth();
            int height = texture.getHeight();
            int[] pixels = new int[width * height];
            int[] groups = new int[width * height];
            float[][] sums = new float[PARTS.length][4];
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
            for (int g = 0; g < PARTS.length; g++) {
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
            StockTickerMod.LOGGER.warn("Failed to read the stock ticker texture for recolouring", e);
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
         int bestDistance = 48 * 48 * 3;
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
