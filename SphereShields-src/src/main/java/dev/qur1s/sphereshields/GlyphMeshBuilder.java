package dev.qur1s.sphereshields;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side only: builds the same UV-sphere shape as {@link SphereGeometryBridge}, but as plain
 * {@link GlyphFace} triangles (our own type, no reflection needed) with a deterministic per-face
 * glyph index, for {@code dev.qur1s.sphereshields.mixin.GlyphOverlayRenderMixin} to draw as a
 * textured overlay on top of the vanilla shield mesh.
 */
public final class GlyphMeshBuilder {
    private GlyphMeshBuilder() {
    }

    private static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

    public static List<GlyphFace> build(double radius, int rings, int segments) {
        int safeRings = Math.max(2, rings);
        int safeSegments = Math.max(8, segments);
        Vec3[][] ringVertices = new Vec3[safeRings + 1][safeSegments];
        for (int ring = 0; ring <= safeRings; ++ring) {
            double theta = Math.PI * (double) ring / (double) safeRings;
            double horizontal = Math.sin(theta) * radius;
            double y = CENTER.y + Math.cos(theta) * radius;
            for (int segment = 0; segment < safeSegments; ++segment) {
                double angle = 2.0 * Math.PI * (double) segment / (double) safeSegments;
                ringVertices[ring][segment] = new Vec3(CENTER.x + Math.cos(angle) * horizontal, y, CENTER.z + Math.sin(angle) * horizontal);
            }
        }

        List<GlyphFace> faces = new ArrayList<>(safeRings * safeSegments * 2);
        int[] faceCounter = {0};

        Vec3 topPole = ringVertices[0][0];
        for (int segment = 0; segment < safeSegments; ++segment) {
            int nextSegment = (segment + 1) % safeSegments;
            addFace(topPole, ringVertices[1][nextSegment], ringVertices[1][segment], faces, faceCounter);
        }

        for (int ring = 2; ring < safeRings; ++ring) {
            for (int segment = 0; segment < safeSegments; ++segment) {
                int nextSegment = (segment + 1) % safeSegments;
                Vec3 upperCurrent = ringVertices[ring - 1][segment];
                Vec3 upperNext = ringVertices[ring - 1][nextSegment];
                Vec3 lowerCurrent = ringVertices[ring][segment];
                Vec3 lowerNext = ringVertices[ring][nextSegment];
                addFace(upperNext, lowerNext, lowerCurrent, faces, faceCounter);
                addFace(upperNext, lowerCurrent, upperCurrent, faces, faceCounter);
            }
        }

        Vec3 bottomPole = ringVertices[safeRings][0];
        for (int segment = 0; segment < safeSegments; ++segment) {
            int nextSegment = (segment + 1) % safeSegments;
            addFace(bottomPole, ringVertices[safeRings - 1][segment], ringVertices[safeRings - 1][nextSegment], faces, faceCounter);
        }

        return faces;
    }

    private static void addFace(Vec3 a, Vec3 b, Vec3 c, List<GlyphFace> faces, int[] faceCounter) {
        Vec3 ab = b.subtract(a);
        Vec3 ac = c.subtract(a);
        Vec3 cross = ab.cross(ac);
        if (cross.lengthSqr() < 1.0E-8) return;

        Vec3 centroid = new Vec3((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0, (a.z + b.z + c.z) / 3.0);
        Vec3 outward = centroid.subtract(CENTER);
        boolean facingOutward = cross.dot(outward) >= 0.0;

        int index = faceCounter[0]++;
        int glyphIndex = Math.floorMod(index * 7919, ShieldGlyphs.COUNT);
        faces.add(facingOutward ? new GlyphFace(a, b, c, glyphIndex) : new GlyphFace(a, c, b, glyphIndex));
    }
}
