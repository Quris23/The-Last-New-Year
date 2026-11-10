import com.google.gson.*;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Part mask of the Crane implant texture for recolouring.
 *  usage: MaskPaint <texture.png> <paint.json> <out_mask.png>
 *  Rectangles of paint.json (the implant's own faces) take the part of their material; every other opaque texel
 *  (the Crane's own texture) is sorted by colour: brass = receiver, brown = handguard, light grey = barrel,
 *  dark grey = sights, copper = handle, green = lights. */
public class MaskPaint {
    // receiver, handguard, barrel, sights, handle, lights, hull, fittings
    static final int[] PALETTE = {0xFFD800, 0xA0522D, 0x8FA3B8, 0x5A3A8C, 0xFF7A00, 0x00FF66, 0xFF00FF, 0x00B7FF};
    static final Map<String, Integer> MATERIAL = Map.of("hull", 6, "hull_dark", 6, "bay", 6, "steel", 7, "steel_dark", 7, "brass", 7, "light", 5);

    static int classify(int rgb) {
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        float[] hsv = Color.RGBtoHSB(r, g, b, null);
        float hue = hsv[0] * 360, sat = hsv[1], val = hsv[2] * 255;
        if (sat < 0.15f) {
            if (val > 235 && hue > 20 && hue < 60) return 0; // highlight on brass
            return val >= 86 ? 2 : 3;
        }
        if (hue >= 70 && hue <= 170) return 5;
        if (hue >= 27 && hue < 70) return val >= 120 ? 0 : 1;
        return val >= 135 ? 4 : 1;
    }

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int w = src.getWidth(), h = src.getHeight();
        int[] group = new int[w * h];
        Arrays.fill(group, -1);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                if ((p >>> 24) > 0) group[y * w + x] = classify(p);
            }
        JsonObject spec = JsonParser.parseString(Files.readString(Path.of(a[1]), StandardCharsets.UTF_8)).getAsJsonObject();
        for (JsonElement e : spec.getAsJsonArray("rects")) {
            JsonObject o = e.getAsJsonObject();
            JsonArray r = o.getAsJsonArray("rect");
            int g = MATERIAL.get(o.get("mat").getAsString());
            for (int j = 0; j < r.get(3).getAsInt(); j++)
                for (int i = 0; i < r.get(2).getAsInt(); i++)
                    group[(r.get(1).getAsInt() + j) * w + r.get(0).getAsInt() + i] = g;
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] count = new int[PALETTE.length];
        for (int i = 0; i < group.length; i++)
            if (group[i] >= 0) {
                out.setRGB(i % w, i / w, 0xFF000000 | PALETTE[group[i]]);
                count[group[i]]++;
            }
        ImageIO.write(out, "png", new File(a[2]));
        System.out.println("mask " + Arrays.toString(count));
    }
}
