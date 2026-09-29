package dev.qur1s.sphereshields;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * shield_generators' {@code ShieldGeometry.Bounds} and {@code ShieldTriangle} are package-private
 * with no public factory. Putting our own classes in {@code com.anton.shieldgenerators} (same
 * package name, direct access, no reflection) used to work at the classloader level but NeoForge
 * 1.21+ runs each mod jar as its own Java module - two modules exporting the same package name is a
 * "split package" and the module resolver refuses to start the game at all. Reflection is the only
 * way left to construct these two types from outside shield_generators' own jar/module.
 */
public final class SphereGeometryBridge {
    private SphereGeometryBridge() {
    }

    private static final Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

    private static final Constructor<?> TRIANGLE_CTOR;
    private static final Constructor<?> BOUNDS_CTOR;

    static {
        try {
            Class<?> triangleClass = Class.forName("com.anton.shieldgenerators.ShieldTriangle");
            Constructor<?> triangleCtor = triangleClass.getDeclaredConstructor(Vec3.class, Vec3.class, Vec3.class);
            triangleCtor.setAccessible(true);
            TRIANGLE_CTOR = triangleCtor;

            Class<?> boundsClass = Class.forName("com.anton.shieldgenerators.ShieldGeometry$Bounds");
            Constructor<?> boundsCtor = boundsClass.getDeclaredConstructor(
                    boolean.class, AABB.class, AABB.class, List.class, List.class, List.class, List.class, double.class);
            boundsCtor.setAccessible(true);
            BOUNDS_CTOR = boundsCtor;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("SphereShields: shield_generators internals changed shape, can't bind to them", e);
        }
    }

    /** Returns a {@code ShieldGeometry.Bounds} instance (typed Object - the real class isn't accessible here). */
    public static Object staticSphereBounds(BlockPos generatorPos, double radius) {
        double clampedRadius = Math.max(1.0, radius);
        List<Object> renderTriangles = sphereTriangles(clampedRadius, 24, 32);
        List<Object> collisionTriangles = sphereTriangles(clampedRadius, 12, 24);
        AABB renderBounds = new AABB(
                CENTER.x - clampedRadius, CENTER.y - clampedRadius, CENTER.z - clampedRadius,
                CENTER.x + clampedRadius, CENTER.y + clampedRadius, CENTER.z + clampedRadius);
        AABB worldBounds = renderBounds.move(generatorPos);
        double surfaceArea = 4.0 * Math.PI * clampedRadius * clampedRadius;
        try {
            return BOUNDS_CTOR.newInstance(true, renderBounds, worldBounds, List.of(), renderTriangles, List.of(), collisionTriangles, surfaceArea);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** UV sphere: {@code rings} latitude steps from the north pole (theta=0) to the south pole (theta=PI). */
    private static List<Object> sphereTriangles(double radius, int rings, int segments) {
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

        ArrayList<Object> triangles = new ArrayList<>(safeRings * safeSegments * 2);

        Vec3 topPole = ringVertices[0][0];
        for (int segment = 0; segment < safeSegments; ++segment) {
            int nextSegment = (segment + 1) % safeSegments;
            addOrientedTriangle(topPole, ringVertices[1][nextSegment], ringVertices[1][segment], triangles);
        }

        for (int ring = 2; ring < safeRings; ++ring) {
            for (int segment = 0; segment < safeSegments; ++segment) {
                int nextSegment = (segment + 1) % safeSegments;
                Vec3 upperCurrent = ringVertices[ring - 1][segment];
                Vec3 upperNext = ringVertices[ring - 1][nextSegment];
                Vec3 lowerCurrent = ringVertices[ring][segment];
                Vec3 lowerNext = ringVertices[ring][nextSegment];
                addOrientedTriangle(upperNext, lowerNext, lowerCurrent, triangles);
                addOrientedTriangle(upperNext, lowerCurrent, upperCurrent, triangles);
            }
        }

        Vec3 bottomPole = ringVertices[safeRings][0];
        for (int segment = 0; segment < safeSegments; ++segment) {
            int nextSegment = (segment + 1) % safeSegments;
            addOrientedTriangle(bottomPole, ringVertices[safeRings - 1][segment], ringVertices[safeRings - 1][nextSegment], triangles);
        }

        return triangles;
    }

    private static void addOrientedTriangle(Vec3 a, Vec3 b, Vec3 c, List<Object> triangles) {
        Vec3 ab = b.subtract(a);
        Vec3 ac = c.subtract(a);
        Vec3 cross = ab.cross(ac);
        if (cross.lengthSqr() < 1.0E-8) return;

        Vec3 centroid = new Vec3((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0, (a.z + b.z + c.z) / 3.0);
        Vec3 outward = centroid.subtract(CENTER);
        boolean facingOutward = cross.dot(outward) >= 0.0;
        try {
            triangles.add(facingOutward
                    ? TRIANGLE_CTOR.newInstance(a, b, c)
                    : TRIANGLE_CTOR.newInstance(a, c, b));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
