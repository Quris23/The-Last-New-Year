package dev.qur1s.atlas.data;

/**
 * One map sheet of an Atlas: a vanilla map's picture (128x128 pixels of map colour ids, 0 = nothing there) with the
 * map's scale and centre. A pixel covers 2^scale blocks, so a smaller scale is a more detailed picture, and the
 * detailed sheets are drawn over the coarse ones.
 */
public final class AtlasSheet {
    public static final int SIZE = 128;
    public static final int AREA = SIZE * SIZE;

    public final int scale;
    public final int centreX;
    public final int centreZ;
    public final byte[] colors;

    /** Order in which maps were poured in (0 = not a poured map, e.g. made by "load the whole map"); the highest is the last. */
    public long seq;
    /** The id of the map item made for this sheet when someone looked at taking it out again (-1 = none yet). */
    public int pendingMapId = -1;

    /** The vanilla map data that keeps this sheet drawn up while a player stands on it (shares {@link #colors}). */
    public net.minecraft.world.level.saveddata.maps.MapItemSavedData live;

    private int cachedCoverage = -1;

    public AtlasSheet(int scale, int centreX, int centreZ) {
        this(scale, centreX, centreZ, new byte[AREA]);
    }

    public AtlasSheet(int scale, int centreX, int centreZ, byte[] colors) {
        this.scale = scale;
        this.centreX = centreX;
        this.centreZ = centreZ;
        this.colors = colors;
    }

    public record Key(int scale, int centreX, int centreZ) {
    }

    public Key key() {
        return new Key(scale, centreX, centreZ);
    }

    /** How many blocks one side of the sheet covers. */
    public int blocks() {
        return SIZE << scale;
    }

    public void invalidate() {
        cachedCoverage = -1;
    }

    /** Roughly how many 16x16-block chunks have something drawn on this sheet. */
    public int coverage() {
        if (cachedCoverage >= 0) return cachedCoverage;
        int count = 0;
        if (scale >= 4) {
            int perPixel = (1 << scale) / 16;
            for (byte b : colors) if ((b & 0xFF) >> 2 != 0) count += perPixel * perPixel;
        } else {
            int cell = 16 >> scale;      // pixels along one chunk
            for (int cz = 0; cz < SIZE; cz += cell) {
                for (int cx = 0; cx < SIZE; cx += cell) {
                    boolean any = false;
                    for (int z = 0; z < cell && !any; z++) {
                        for (int x = 0; x < cell; x++) {
                            if (((colors[cx + x + (cz + z) * SIZE] & 0xFF) >> 2) != 0) {
                                any = true;
                                break;
                            }
                        }
                    }
                    if (any) count++;
                }
            }
        }
        return cachedCoverage = count;
    }

    /** Draws {@code other}'s pixels over this sheet's where it has any. */
    public void overlay(AtlasSheet other) {
        for (int i = 0; i < AREA; i++) {
            if (((other.colors[i] & 0xFF) >> 2) != 0) colors[i] = other.colors[i];
        }
        invalidate();
    }
}
