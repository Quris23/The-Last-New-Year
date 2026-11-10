import com.google.gson.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Random;

/** Paints the texture of a generated ammo box model.
 *  usage: AmmoPaint <paint.json> <out.png>
 *  paint.json: {"size": n, "palette": {name: {"fill","border","hi"}}, "rects": [{"rect":[x,y,w,h], "mat": name, "image"?: png}]}
 *  A rect gets a 1 texel border, a highlight on its second row and column (when big enough) and light noise in the fill.
 *  With "image", that picture is drawn 1:1 over the middle of the rect (the label of the box). */
public class AmmoPaint {
    static int rgb(String h) { return 0xFF000000 | Integer.parseInt(h, 16); }

    static int shade(int c, double f) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r = (int) Math.max(0, Math.min(255, Math.round(r * f)));
        g = (int) Math.max(0, Math.min(255, Math.round(g * f)));
        b = (int) Math.max(0, Math.min(255, Math.round(b * f)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void main(String[] a) throws Exception {
        JsonObject spec = JsonParser.parseString(Files.readString(Path.of(a[0]), StandardCharsets.UTF_8)).getAsJsonObject();
        int size = spec.get("size").getAsInt();
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        JsonObject pal = spec.getAsJsonObject("palette");
        Random rnd = new Random(11);
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
            if (o.has("image")) {
                BufferedImage label = ImageIO.read(new File(o.get("image").getAsString()));
                int lw = label.getWidth(), lh = label.getHeight();
                int ox = x + (w - lw) / 2, oy = y + (h - lh) / 2;
                for (int j = 0; j < lh; j++)
                    for (int i = 0; i < lw; i++) {
                        int px = ox + i, py = oy + j;
                        if (px <= x || py <= y || px >= x + w - 1 || py >= y + h - 1) continue;
                        int p = label.getRGB(i, j);
                        if ((p >>> 24) >= 128) img.setRGB(px, py, 0xFF000000 | p);
                    }
            }
        }
        new File(a[1]).getParentFile().mkdirs();
        ImageIO.write(img, "png", new File(a[1]));
    }
}
