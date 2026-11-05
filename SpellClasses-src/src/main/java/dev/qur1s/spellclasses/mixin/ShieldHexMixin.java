package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Magic Shield (Хекс-кристалл «Магический щит»): instead of Iron's 5x5 square of half-block parts, the shield is a regular
 * hexagon (flat top, circumradius 1.1) built from quarter-block parts that block spells, as Iron's own shield parts do.
 */
@Mixin(value = ShieldEntity.class, remap = false)
public abstract class ShieldHexMixin {
    @Shadow protected ShieldPart[] subEntities;
    @Shadow @Final @Mutable protected Vec3[] subPositions;

    private static final double RADIUS = 1.1;
    private static final double CELL = 0.25;
    private static final double SQRT3_2 = 0.8660254037844386;

    @Inject(method = "createShield", at = @At("RETURN"), require = 0)
    private void spellclasses$hexParts(CallbackInfo ci) {
        ShieldEntity self = (ShieldEntity) (Object) this;
        List<ShieldPart> parts = new ArrayList<>();
        List<Vec3> positions = new ArrayList<>();
        int n = (int) Math.ceil(RADIUS / CELL);
        double apothem = RADIUS * SQRT3_2;
        for (int ix = -n; ix <= n; ix++) {
            for (int iy = -n; iy <= n; iy++) {
                double x = ix * CELL;
                double y = iy * CELL;
                if (Math.abs(y) > apothem || Math.abs(x) * SQRT3_2 + Math.abs(y) * 0.5 > apothem) continue;
                parts.add(new ShieldPart(self, "shield_hex_" + parts.size(), (float) CELL, (float) CELL, true));
                // a part's position is its feet: its centre is half a cell higher
                positions.add(new Vec3(x, y - CELL / 2, 0));
            }
        }
        this.subEntities = parts.toArray(new ShieldPart[0]);
        this.subPositions = positions.toArray(new Vec3[0]);
    }
}
