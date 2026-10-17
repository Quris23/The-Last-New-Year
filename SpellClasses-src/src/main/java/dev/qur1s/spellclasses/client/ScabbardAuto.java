package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Works out, from a model's own geometry, how to lay an item on the back without any hand-made tuning: finds its long axis
 * (principal component of the vertices), its thin axis, which end is the grip/head, and returns the rotation that puts the
 * long axis on the shoulder-to-hip diagonal with the thin axis pointing out of the back (so it lies flat against the body).
 * Staves and polearms go head to the right shoulder; everything else goes handle to the right shoulder.
 */
final class ScabbardAuto {
    /**
     * @param center       middle of the item, in the model space the renderer draws in (after the FIXED display transform)
     * @param rotation     turns the model's long axis onto the shoulder end direction and the thin axis onto the back normal
     * @param length       length along the long axis in that space, blocks
     * @param handLength   the same length as seen in the hand (THIRD_PERSON_RIGHT_HAND), blocks
     * @param thickness    extent along the thin axis in that space, blocks
     */
    record Fit(Vector3f center, Quaternionf rotation, float length, float handLength, float thickness) {
    }

    private static final Map<BakedModel, Fit> CACHE_STAFF = new WeakHashMap<>();
    private static final Map<BakedModel, Fit> CACHE_OTHER = new WeakHashMap<>();
    private static final Fit NONE = new Fit(new Vector3f(), new Quaternionf(), 0f, 0f, 0f);

    /** Direction of the shoulder end in the body model's space: towards the wearer's right (-x) and up (-y). */
    private static final Vector3f SHOULDER_DIR = new Vector3f(-0.70710678f, -0.70710678f, 0f);

    private ScabbardAuto() {
    }

    /** The fit for this model, or {@code null} if it has too little geometry to tell. */
    static Fit fitFor(BakedModel model, boolean staffLike) {
        Map<BakedModel, Fit> cache = staffLike ? CACHE_STAFF : CACHE_OTHER;
        Fit fit = cache.get(model);
        if (fit == null) {
            try {
                fit = compute(model, staffLike);
            } catch (RuntimeException e) {
                fit = null;
            }
            cache.put(model, fit == null ? NONE : fit);
        }
        return fit == null || fit == NONE ? null : fit;
    }

    private static Matrix4f transform(BakedModel model, ItemDisplayContext context) {
        PoseStack stack = new PoseStack();
        model.applyTransform(context, stack, false);
        return new Matrix4f(stack.last().pose());
    }

    private static Fit compute(BakedModel model, boolean staffLike) {
        Matrix4f fixed = transform(model, ItemDisplayContext.FIXED);
        Matrix4f hand = transform(model, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        List<Vector3f> p = new ArrayList<>();
        List<Vector3f> h = new ArrayList<>();
        RandomSource random = RandomSource.create(42L);
        for (Direction side : new Direction[]{null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH,
                Direction.EAST, Direction.WEST}) {
            for (BakedQuad quad : model.getQuads(null, side, random)) {
                int[] v = quad.getVertices();
                for (int i = 0; i + 2 < v.length; i += 8) {
                    Vector3f raw = new Vector3f(Float.intBitsToFloat(v[i]) - 0.5f, Float.intBitsToFloat(v[i + 1]) - 0.5f,
                            Float.intBitsToFloat(v[i + 2]) - 0.5f);
                    p.add(fixed.transformPosition(new Vector3f(raw), new Vector3f()));
                    h.add(hand.transformPosition(new Vector3f(raw), new Vector3f()));
                }
            }
        }
        if (p.size() < 8) return null;

        Vector3f mean = new Vector3f();
        for (Vector3f v : p) mean.add(v);
        mean.div(p.size());

        // Covariance matrix and its principal axes (Jacobi eigenvalue iteration on a 3x3 symmetric matrix).
        double[][] cov = new double[3][3];
        for (Vector3f v : p) {
            double[] d = {v.x - mean.x, v.y - mean.y, v.z - mean.z};
            for (int a = 0; a < 3; a++) for (int b = 0; b < 3; b++) cov[a][b] += d[a] * d[b];
        }
        double[][] vec = new double[3][3];
        double[] val = new double[3];
        jacobi(cov, vec, val);
        int iMax = 0, iMin = 0;
        for (int i = 1; i < 3; i++) {
            if (val[i] > val[iMax]) iMax = i;
            if (val[i] < val[iMin]) iMin = i;
        }
        if (iMax == iMin || val[iMax] < 1e-9) return null;
        Vector3f axis = new Vector3f((float) vec[0][iMax], (float) vec[1][iMax], (float) vec[2][iMax]).normalize();
        Vector3f thin = new Vector3f((float) vec[0][iMin], (float) vec[1][iMin], (float) vec[2][iMin]).normalize();

        float tMin = Float.MAX_VALUE, tMax = -Float.MAX_VALUE;
        int iLo = 0, iHi = 0;
        float[] t = new float[p.size()];
        for (int i = 0; i < p.size(); i++) {
            t[i] = new Vector3f(p.get(i)).sub(mean).dot(axis);
            if (t[i] < tMin) {
                tMin = t[i];
                iLo = i;
            }
            if (t[i] > tMax) {
                tMax = t[i];
                iHi = i;
            }
        }
        float length = tMax - tMin;
        if (length < 1e-3f) return null;

        // Which end goes to the shoulder. Staves: the wider (head) end. Everything else: the end that was in the hand.
        boolean shoulderIsHigh;
        if (staffLike) {
            shoulderIsHigh = endRadius(p, t, mean, axis, tMax - 0.3f * length, tMax) >= endRadius(p, t, mean, axis, tMin, tMin + 0.3f * length);
        } else {
            shoulderIsHigh = h.get(iHi).length() <= h.get(iLo).length();
        }
        if (!shoulderIsHigh) {
            axis.negate();
            float swap = tMin;
            tMin = -tMax;
            tMax = -swap;
        }

        float thinMin = Float.MAX_VALUE, thinMax = -Float.MAX_VALUE;
        for (Vector3f v : p) {
            float d = new Vector3f(v).sub(mean).dot(thin);
            thinMin = Math.min(thinMin, d);
            thinMax = Math.max(thinMax, d);
        }

        // Source frame (long axis, side axis, thin axis) and target frame (shoulder direction, its side, back normal).
        Vector3f side = new Vector3f(thin).cross(axis).normalize();
        Matrix3f source = new Matrix3f().set(axis, side, thin);
        Vector3f backNormal = new Vector3f(0f, 0f, 1f);
        Vector3f targetSide = new Vector3f(backNormal).cross(SHOULDER_DIR).normalize();
        Matrix3f target = new Matrix3f().set(SHOULDER_DIR, targetSide, backNormal);
        Matrix3f rot = new Matrix3f(target).mul(new Matrix3f(source).transpose());
        Quaternionf q = new Quaternionf().setFromNormalized(rot).normalize();

        Vector3f center = new Vector3f(mean).add(new Vector3f(axis).mul((tMin + tMax) / 2f));

        // The length in the hand: distance between the same two extreme vertices in hand space.
        float handLength = h.get(iHi).distance(h.get(iLo));
        if (handLength < 1e-3f) handLength = length;
        return new Fit(center, q, length, handLength, thinMax - thinMin);
    }

    /** Largest distance from the long axis among the vertices whose position along it lies in [from, to]. */
    private static float endRadius(List<Vector3f> p, float[] t, Vector3f mean, Vector3f axis, float from, float to) {
        float best = 0f;
        for (int i = 0; i < p.size(); i++) {
            if (t[i] < from || t[i] > to) continue;
            Vector3f d = new Vector3f(p.get(i)).sub(mean);
            d.sub(new Vector3f(axis).mul(t[i]));
            best = Math.max(best, d.length());
        }
        return best;
    }

    private static void jacobi(double[][] a, double[][] v, double[] d) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) v[i][j] = i == j ? 1 : 0;
        }
        for (int sweep = 0; sweep < 50; sweep++) {
            double off = Math.abs(a[0][1]) + Math.abs(a[0][2]) + Math.abs(a[1][2]);
            if (off < 1e-14) break;
            for (int pI = 0; pI < 2; pI++) {
                for (int qI = pI + 1; qI < 3; qI++) {
                    if (Math.abs(a[pI][qI]) < 1e-18) continue;
                    double theta = (a[qI][qI] - a[pI][pI]) / (2 * a[pI][qI]);
                    double tt = Math.signum(theta) / (Math.abs(theta) + Math.sqrt(theta * theta + 1));
                    if (theta == 0) tt = 1;
                    double c = 1 / Math.sqrt(tt * tt + 1), s = tt * c;
                    for (int k = 0; k < 3; k++) {
                        double akp = a[k][pI], akq = a[k][qI];
                        a[k][pI] = c * akp - s * akq;
                        a[k][qI] = s * akp + c * akq;
                    }
                    for (int k = 0; k < 3; k++) {
                        double apk = a[pI][k], aqk = a[qI][k];
                        a[pI][k] = c * apk - s * aqk;
                        a[qI][k] = s * apk + c * aqk;
                    }
                    for (int k = 0; k < 3; k++) {
                        double vkp = v[k][pI], vkq = v[k][qI];
                        v[k][pI] = c * vkp - s * vkq;
                        v[k][qI] = s * vkp + c * vkq;
                    }
                }
            }
        }
        for (int i = 0; i < 3; i++) d[i] = a[i][i];
    }
}
