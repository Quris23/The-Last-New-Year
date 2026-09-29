package dev.qur1s.sphereshields;

import net.minecraft.world.phys.Vec3;

/** One triangular face of the dome mesh, tagged with which glyph texture to draw on it. */
public record GlyphFace(Vec3 a, Vec3 b, Vec3 c, int glyphIndex) {
}
