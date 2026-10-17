package dev.qur1s.atlas.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.qur1s.atlas.data.AtlasLiveEntry;
import dev.qur1s.atlas.data.AtlasMarker;
import dev.qur1s.atlas.data.AtlasSheet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The tiles of the open Atlas as GPU textures, filled as the server streams them in. */
public final class ClientAtlasCache {
    /** A map sheet as a GPU texture, with where it lies, its pixels and where the stitches along its edge go. */
    public static final class Sheet {
        private final int scale, centreX, centreZ;
        private final DynamicTexture texture;
        private final ResourceLocation location;
        private byte[] colors = new byte[AtlasSheet.AREA];
        private float[] anchors = new float[0];
        private int anchorsVersion = -1;

        Sheet(int scale, int centreX, int centreZ, DynamicTexture texture, ResourceLocation location) {
            this.scale = scale;
            this.centreX = centreX;
            this.centreZ = centreZ;
            this.texture = texture;
            this.location = location;
        }

        public int scale() {
            return scale;
        }

        public int centreX() {
            return centreX;
        }

        public int centreZ() {
            return centreZ;
        }

        public DynamicTexture texture() {
            return texture;
        }

        public ResourceLocation location() {
            return location;
        }

        public int blocks() {
            return AtlasSheet.SIZE << scale;
        }

        private boolean filled(int px, int pz) {
            return px >= 0 && pz >= 0 && px < AtlasSheet.SIZE && pz < AtlasSheet.SIZE
                    && ((colors[px + pz * AtlasSheet.SIZE] & 0xFF) >> 2) != 0;
        }

        /** Whether this sheet has something drawn at the block (x, z). */
        public boolean coversBlock(double x, double z) {
            int half = blocks() / 2;
            int px = (int) Math.floor((x - (centreX - half)) / (1 << scale));
            int pz = (int) Math.floor((z - (centreZ - half)) / (1 << scale));
            return filled(px, pz);
        }

        /**
         * Where this sheet is sewn to another: along the edge of what is drawn on it, where the other side is drawn
         * on a coarser sheet (it lies on that) or on the next sheet of the same scale. Groups of four numbers: the
         * block position of the seam, and which way is out (dx, dz).
         */
        public float[] stitches() {
            if (anchorsVersion == version) return anchors;
            anchorsVersion = version;
            java.util.List<Float> out = new java.util.ArrayList<>();
            int half = blocks() / 2, size = AtlasSheet.SIZE, pixel = 1 << scale;
            double left = centreX - half, top = centreZ - half;
            int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            for (int pz = 0; pz < size; pz++) {
                for (int px = 0; px < size; px++) {
                    if (!filled(px, pz)) continue;
                    for (int[] d : dirs) {
                        boolean alongX = d[1] != 0;                   // a seam that runs along x has its stitches spread along x
                        int along = alongX ? px : pz;
                        if (along % 16 != 8) continue;                // one stitch every 16 map pixels
                        int nx = px + d[0], nz = pz + d[1];
                        if (filled(nx, nz)) continue;                 // not an edge of what is drawn here
                        double wx = left + (nx + 0.5) * pixel, wz = top + (nz + 0.5) * pixel;
                        boolean sewn = false;
                        if (nx < 0 || nz < 0 || nx >= size || nz >= size) {
                            // off this sheet: the next sheet of the same scale (sewn from the first of the two)
                            if (d[0] + d[1] > 0) {
                                Sheet next = SHEETS.get(new AtlasSheet.Key(scale, centreX + d[0] * blocks(), centreZ + d[1] * blocks()));
                                sewn = next != null && next.coversBlock(wx, wz);
                            }
                        }
                        // a coarser sheet this one lies on (the list is ordered coarse first)
                        for (Sheet other : drawOrder) {
                            if (sewn || other.scale <= scale) break;
                            sewn = other.coversBlock(wx, wz);
                        }
                        if (sewn) {
                            out.add((float) (left + (px + 0.5 + d[0] * 0.5) * pixel));
                            out.add((float) (top + (pz + 0.5 + d[1] * 0.5) * pixel));
                            out.add((float) d[0]);
                            out.add((float) d[1]);
                        }
                    }
                }
            }
            anchors = new float[out.size()];
            for (int i = 0; i < anchors.length; i++) anchors[i] = out.get(i);
            return anchors;
        }
    }

    /** Bumped whenever a sheet arrives, so the stitches are worked out again. */
    private static int version;

    private static UUID currentAtlas;
    private static String currentDimension = "";
    private static final Map<AtlasSheet.Key, Sheet> SHEETS = new HashMap<>();
    private static List<Sheet> drawOrder = new ArrayList<>();
    private static List<AtlasMarker> markers = new ArrayList<>();
    private static List<String> dimensions = new ArrayList<>();
    private static List<AtlasLiveEntry> live = new ArrayList<>();
    private static int[] tracks = new int[0];
    private static List<dev.qur1s.atlas.data.AtlasPlayerEntry> players = new ArrayList<>();

    private ClientAtlasCache() {
    }

    /**
     * Colours in the Atlas's own warm, slightly faded palette instead of the raw vanilla map colours (grass, sand,
     * water, stone, snow, ... get hand-picked tones; everything else is vanilla faded towards parchment).
     */
    private static final int[] SHADE = {180, 220, 255, 135};
    private static final Map<Integer, int[]> PALETTE = new HashMap<>();

    static {
        PALETTE.put(1, new int[]{118, 150, 68});    // grass
        PALETTE.put(2, new int[]{226, 205, 150});   // sand
        PALETTE.put(5, new int[]{176, 200, 210});   // ice
        PALETTE.put(7, new int[]{82, 128, 52});     // plants / leaves
        PALETTE.put(8, new int[]{240, 236, 226});   // snow
        PALETTE.put(9, new int[]{160, 166, 180});   // clay
        PALETTE.put(10, new int[]{140, 100, 66});   // dirt
        PALETTE.put(11, new int[]{130, 126, 118});  // stone
        PALETTE.put(12, new int[]{86, 128, 150});   // water
        PALETTE.put(13, new int[]{120, 88, 52});    // wood
    }

    private static int graded(int packed) {
        int base = packed >> 2;
        int shade = SHADE[packed & 3];
        int[] rgb = PALETTE.get(base);
        int r, g, b;
        if (rgb != null) {
            r = rgb[0];
            g = rgb[1];
            b = rgb[2];
        } else {
            int abgr = MapColor.getColorFromPackedId(base << 2 | 2);   // the base colour at full brightness
            r = abgr & 0xFF;
            g = (abgr >> 8) & 0xFF;
            b = (abgr >> 16) & 0xFF;
            int grey = (r * 3 + g * 6 + b) / 10;
            r = (r * 3 + grey) / 4;
            g = (g * 3 + grey) / 4;
            b = (b * 3 + grey) / 4;
        }
        // warm, slightly faded
        r = Math.min(255, r * shade / 255 + 6);
        g = Math.min(255, g * shade / 255 + 3);
        b = Math.min(255, b * shade / 255 - 4 < 0 ? 0 : b * shade / 255 - 4);
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    /** The sheets, coarse ones first, so that drawing them in this order puts the detailed ones on top. */
    public static List<Sheet> sheets() {
        return drawOrder;
    }

    public static Map<AtlasSheet.Key, Sheet> sheetMap() {
        return SHEETS;
    }

    private static void reorder() {
        drawOrder = new ArrayList<>(SHEETS.values());
        drawOrder.sort(java.util.Comparator.comparingInt((Sheet sheet) -> -sheet.scale())
                .thenComparingInt(Sheet::centreZ).thenComparingInt(Sheet::centreX));
    }

    public static List<dev.qur1s.atlas.data.AtlasPlayerEntry> players() {
        return players;
    }

    public static void setPlayers(List<dev.qur1s.atlas.data.AtlasPlayerEntry> list) {
        players = new ArrayList<>(list);
    }

    public static int[] tracks() {
        return tracks;
    }

    public static void setTracks(UUID atlasId, String dimension, int[] segments) {
        if (atlasId.equals(currentAtlas) && dimension.equals(currentDimension)) tracks = segments;
    }

    public static List<AtlasLiveEntry> live() {
        return live;
    }

    public static void setLive(UUID atlasId, String dimension, List<AtlasLiveEntry> list) {
        if (atlasId.equals(currentAtlas) && dimension.equals(currentDimension)) live = new ArrayList<>(list);
    }

    public static List<String> dimensions() {
        return dimensions;
    }

    public static void setDimensions(UUID atlasId, List<String> list) {
        if (atlasId.equals(currentAtlas)) dimensions = new ArrayList<>(list);
    }

    public static List<AtlasMarker> markers() {
        return markers;
    }

    public static void setMarkers(UUID atlasId, String dimension, List<AtlasMarker> list) {
        if (atlasId.equals(currentAtlas) && dimension.equals(currentDimension)) markers = new ArrayList<>(list);
    }

    /** Forgets everything and starts collecting tiles for another atlas or dimension. */
    public static void reset(UUID atlasId, String dimension) {
        clear();
        if (atlasId == null || !atlasId.equals(currentAtlas)) dimensions = new ArrayList<>();
        currentAtlas = atlasId;
        currentDimension = dimension;
    }

    public static void clear() {
        var textures = Minecraft.getInstance().getTextureManager();
        SHEETS.values().forEach(sheet -> textures.release(sheet.location()));
        SHEETS.clear();
        drawOrder = new ArrayList<>();
        version++;
        markers = new ArrayList<>();
        live = new ArrayList<>();
        tracks = new int[0];
    }

    public static void put(UUID atlasId, String dimension, int scale, int centreX, int centreZ, byte[] colors) {
        if (!atlasId.equals(currentAtlas) || !dimension.equals(currentDimension) || colors.length != AtlasSheet.AREA) return;
        AtlasSheet.Key key = new AtlasSheet.Key(scale, centreX, centreZ);
        Sheet sheet = SHEETS.get(key);
        if (sheet == null) {
            DynamicTexture texture = new DynamicTexture(AtlasSheet.SIZE, AtlasSheet.SIZE, true);
            ResourceLocation location = Minecraft.getInstance().getTextureManager()
                    .register("atlas_sheet_" + scale + "_" + centreX + "_" + centreZ, texture);
            sheet = new Sheet(scale, centreX, centreZ, texture, location);
            SHEETS.put(key, sheet);
            reorder();
        }
        sheet.colors = colors;
        version++;
        NativeImage pixels = sheet.texture().getPixels();
        for (int z = 0; z < AtlasSheet.SIZE; z++) {
            for (int x = 0; x < AtlasSheet.SIZE; x++) {
                int packed = colors[x + z * AtlasSheet.SIZE] & 0xFF;
                pixels.setPixelRGBA(x, z, packed >> 2 == 0 ? 0 : graded(packed));
            }
        }
        sheet.texture().upload();
    }
}
