import com.google.gson.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Random;

/** Copies a texture and paints per-face rectangles with flat materials.
 *  usage: TexPaint <src.png> <paint.json> <out.png>
 *  paint.json: {"palette": {name: {"fill","border","hi","rivets"?}}, "rects": [{"rect":[x,y,w,h] (texels), "mat": name}]}
 *  A rect gets a 1 texel border, a 1 texel highlight on its top row and left column (when it is big enough),
 *  light noise in the fill, and rivets in the inner corners on big rects when the material has them. */
public class TexPaint {
    static int rgb(String h) { return 0xFF000000 | Integer.parseInt(h, 16); }

    static int shade(int c, double f) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r = (int) Math.max(0, Math.min(255, Math.round(r * f)));
        g = (int) Math.max(0, Math.min(255, Math.round(g * f)));
        b = (int) Math.max(0, Math.min(255, Math.round(b * f)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(src, 0, 0, null);
        JsonObject spec = JsonParser.parseString(Files.readString(Path.of(a[1]), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject pal = spec.getAsJsonObject("palette");
        Random rnd = new Random(7);
        for (JsonElement e : spec.getAsJsonArray("rects")) {
            JsonObject o = e.getAsJsonObject();
            JsonArray r = o.getAsJsonArray("rect");
            int x = r.get(0).getAsInt(), y = r.get(1).getAsInt(), w = r.get(2).getAsInt(), h = r.get(3).getAsInt();
            JsonObject m = pal.getAsJsonObject(o.get("mat").getAsString());
            int fill = rgb(m.get("fill").getAsString()), border = rgb(m.get("border").getAsString()), hi = rgb(m.get("hi").getAsString());
            for (int j = 0; j < h; j++)
                for (int i = 0; i < w; i++) {
                    int c;
                    boolean edgeX = w >= 3 && (i == 0 || i == w - 1), edgeY = h >= 3 && (j == 0 || j == h - 1);
                    if (w < 3 && h < 3) c = fill;
                    else if (edgeX || edgeY) c = border;
                    else if ((w >= 5 && i == 1) || (h >= 5 && j == 1)) c = hi;
                    else c = shade(fill, 0.95 + rnd.nextDouble() * 0.08);
                    img.setRGB(x + i, y + j, c);
                }
            if (m.has("rivets") && w >= 12 && h >= 12) {
                int rv = rgb(m.get("rivets").getAsString());
                int[][] pts = {{3, 3}, {w - 4, 3}, {3, h - 4}, {w - 4, h - 4}};
                for (int[] p : pts) img.setRGB(x + p[0], y + p[1], rv);
            }
        }
        ImageIO.write(img, "png", new File(a[2]));
        System.out.println("wrote " + a[2]);
    }
}
