package dev.qur1s.atlas.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sable ships on the Atlas, live: each one is drawn from above out of its real blocks, turned to its heading, with its
 * name beneath. Only ships the client currently knows about (the ones around the player) appear, and only in the
 * dimension the player is standing in. All Sable types stay in this class, which is only touched when Sable is loaded.
 */
final class SableShips {
    private static final int MAX_EDGE = 192;
    private static final long REFRESH_MS = 1500L;

    private static final class ShipTexture {
        DynamicTexture texture;
        ResourceLocation location;
        int width, height;
        long drawnAt;
        boolean seen;
    }

    private static final Map<Integer, ShipTexture> TEXTURES = new HashMap<>();
    private static int counter;

    private SableShips() {
    }

    static void draw(GuiGraphics graphics, Font font, double viewX, double viewZ, double zoom, double centreX, double centreY) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return;
        TEXTURES.values().forEach(t -> t.seen = false);
        long now = System.currentTimeMillis();
        for (ClientSubLevel ship : container.getAllSubLevels()) {
            if (ship == null || ship.isRemoved()) continue;
            BoundingBox3ic box = ship.getPlot().getBoundingBox();
            int w = box.maxX() - box.minX() + 1, h = box.maxZ() - box.minZ() + 1;
            if (w <= 0 || h <= 0 || w > MAX_EDGE || h > MAX_EDGE) continue;

            ShipTexture tex = TEXTURES.computeIfAbsent(System.identityHashCode(ship), k -> new ShipTexture());
            tex.seen = true;
            if (tex.texture == null || tex.width != w || tex.height != h || now - tex.drawnAt > REFRESH_MS) {
                paint(level, box, w, h, tex);
                tex.drawnAt = now;
            }

            Pose3dc pose = ship.renderPose();
            Vector3d centreLocal = new Vector3d((box.minX() + box.maxX() + 1) / 2.0, (box.minY() + box.maxY() + 1) / 2.0,
                    (box.minZ() + box.maxZ() + 1) / 2.0);
            Vector3d world = pose.transformPosition(centreLocal, new Vector3d());
            Vector3d xAxis = pose.orientation().transform(new Vector3d(1, 0, 0), new Vector3d());
            float heading = (float) Math.atan2(xAxis.z, xAxis.x);

            double sx = centreX + (world.x - viewX) * zoom;
            double sy = centreY + (world.z - viewZ) * zoom;
            double radius = Math.max(w, h) * zoom;
            if (sx < -radius || sy < -radius || sx > centreX * 2 + radius || sy > centreY * 2 + radius) continue;

            PoseStack ps = graphics.pose();
            ps.pushPose();
            ps.translate(sx, sy, 0);
            ps.mulPose(Axis.ZP.rotation(heading));
            ps.scale((float) zoom, (float) zoom, 1f);
            graphics.blit(tex.location, -w / 2, -h / 2, w, h, 0, 0, w, h, w, h);
            ps.popPose();

            if (Math.max(w, h) * zoom >= 12) {
                String name = ship.getName();
                String label = name == null || name.isBlank() ? "Корабль" : name;
                AtlasLabels.draw(graphics, font, label, (int) sx, (int) (sy + Math.max(w, h) * zoom / 2 + 3));
            }
        }
        // free the textures of ships that are gone
        Iterator<ShipTexture> it = TEXTURES.values().iterator();
        while (it.hasNext()) {
            ShipTexture t = it.next();
            if (!t.seen) {
                release(t);
                it.remove();
            }
        }
    }

    /** Top-down colours of the ship's blocks: the highest block of each column that has a map colour. */
    private static void paint(ClientLevel level, BoundingBox3ic box, int w, int h, ShipTexture tex) {
        if (tex.texture == null || tex.width != w || tex.height != h) {
            release(tex);
            tex.texture = new DynamicTexture(w, h, true);
            tex.location = Minecraft.getInstance().getTextureManager().register("atlas_ship_" + (counter++), tex.texture);
            tex.width = w;
            tex.height = h;
        }
        NativeImage pixels = tex.texture.getPixels();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < h; z++) {
            for (int x = 0; x < w; x++) {
                int color = 0;
                for (int y = box.maxY(); y >= box.minY(); y--) {
                    pos.set(box.minX() + x, y, box.minZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    MapColor mapColor = state.getMapColor(level, pos);
                    if (mapColor == MapColor.NONE) continue;
                    int abgr = MapColor.getColorFromPackedId(mapColor.getPackedId(MapColor.Brightness.HIGH));
                    // a thin dark outline would hide small ships; a flat colour reads best at this size
                    color = abgr;
                    break;
                }
                pixels.setPixelRGBA(x, z, color);
            }
        }
        tex.texture.upload();
    }

    private static void release(ShipTexture t) {
        if (t.location != null) Minecraft.getInstance().getTextureManager().release(t.location);
        t.texture = null;
        t.location = null;
    }

    static void clear() {
        TEXTURES.values().forEach(SableShips::release);
        TEXTURES.clear();
    }
}
