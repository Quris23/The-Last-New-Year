import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/** Item icon of the Crane implant: the gun's slot icon scaled to 32 px (its pack badge cleared) over a hull cuff.
 *  usage: IconMake <slot.png> <out.png> */
public class IconMake {
    public static void main(String[] a) throws Exception {
        BufferedImage src = ImageIO.read(new File(a[0]));
        int n = 32, sw = src.getWidth(), sh = src.getHeight();
        BufferedImage out = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        // cuff first, the gun over it
        int hull = 0xFF3E3348, edge = 0xFF231E2B, hi = 0xFF574A66, band = 0xFFDCA55B, bandEdge = 0xFF806035;
        for (int y = 17; y <= 27; y++)
            for (int x = 6; x <= 23; x++) {
                int c = hull;
                if (y == 17 || y == 27 || x == 6 || x == 23) c = edge;
                else if (y == 18 || x == 7) c = hi;
                if (x == 10 || x == 18) c = (y == 17 || y == 27) ? bandEdge : band;
                out.setRGB(x, y, c);
            }
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++) {
                long r = 0, g = 0, b = 0, al = 0;
                int cnt = 0;
                for (int sy = y * sh / n; sy < Math.max(y * sh / n + 1, (y + 1) * sh / n); sy++)
                    for (int sx = x * sw / n; sx < Math.max(x * sw / n + 1, (x + 1) * sw / n); sx++) {
                        int p = src.getRGB(sx, sy);
                        int pa = p >>> 24;
                        r += (long) (p >> 16 & 255) * pa; g += (long) (p >> 8 & 255) * pa; b += (long) (p & 255) * pa; al += pa;
                        cnt++;
                    }
                int alpha = (int) (al / cnt);
                if (x < 13 && y < 13) alpha = 0; // the pack badge
                if (alpha >= 128) out.setRGB(x, y, 0xFF000000 | (int) (r / al) << 16 | (int) (g / al) << 8 | (int) (b / al));
            }
        ImageIO.write(out, "png", new File(a[1]));
        System.out.println("icon from " + sw + "x" + sh);
    }
}
