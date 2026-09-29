package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.SignalHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ground-dome-only replacement for shieldColor()/shieldStrength(). Both now come from the same
 * continuous "power coefficient" - {@code (signal/15)^2 x 750}, the same signal-only reduction of
 * {@code redstone x actualRadius} used before, just no longer snapped into hard tiers. Instead each
 * fuel/energy source defines a polyline of (coefficient, color, strength) anchor points by user
 * design, and the live coefficient interpolates linearly between whichever two anchors it falls
 * between - so the dome eases smoothly from one color/toughness into the next as the signal changes,
 * instead of popping between fixed steps.
 * <p>
 * Copper fuel is always pinned to the copper anchor, amethyst fuel always to the amethyst anchor -
 * only lapis, quartz and FE energy actually move along their own anchor polyline as signal changes.
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
abstract class TieredPowerMixin {
    private static final int COPPER_COLOR = 0xFF8A2A;
    private static final int LAPIS_COLOR = 0x2F64FF;
    private static final int QUARTZ_COLOR = 0xF7FBFF;
    private static final int AMETHYST_COLOR = 0xB86CFF;

    private static final double COPPER_STRENGTH = 0.10;
    private static final double LAPIS_STRENGTH = 0.30;
    private static final double QUARTZ_STRENGTH = 0.60;
    private static final double AMETHYST_STRENGTH = 0.80;

    /** Theoretical max of {@code signal x actualRadius} for a 50-block-max generator (15 x 50). */
    private static final double MAX_COEFFICIENT = 750.0;

    private record Stop(double coefficient, int color, double strength) {}

    // FE energy's top anchor only eases a LITTLE toward Lapis past its "pure white" point (500) -
    // by user design it should never look properly blue, just faintly cool.
    private static final double ENERGY_TOP_BLUE_BLEND = 0.2;

    private static final Stop[] LAPIS_PATH = {
            new Stop(200.0, LAPIS_COLOR, LAPIS_STRENGTH),
            new Stop(500.0, COPPER_COLOR, COPPER_STRENGTH),
    };
    private static final Stop[] QUARTZ_PATH = {
            new Stop(150.0, QUARTZ_COLOR, QUARTZ_STRENGTH),
            new Stop(350.0, LAPIS_COLOR, LAPIS_STRENGTH),
            new Stop(750.0, COPPER_COLOR, COPPER_STRENGTH),
    };
    private static final Stop[] ENERGY_PATH = {
            new Stop(50.0, AMETHYST_COLOR, AMETHYST_STRENGTH),
            new Stop(200.0, QUARTZ_COLOR, QUARTZ_STRENGTH),
            new Stop(500.0, QUARTZ_COLOR, QUARTZ_STRENGTH),
            new Stop(750.0, blend(QUARTZ_COLOR, LAPIS_COLOR, ENERGY_TOP_BLUE_BLEND),
                    lerp(QUARTZ_STRENGTH, LAPIS_STRENGTH, ENERGY_TOP_BLUE_BLEND)),
    };

    @Unique
    private boolean sphereshields$poweredByEnergy;

    private static final String POWERED_BY_ENERGY_KEY = "SphereShields_PoweredByEnergy";

    @Inject(method = "tryRunOnEnergy(DIZ)Z", at = @At("RETURN"))
    private void sphereshields$trackEnergyPowered(double surfaceArea, int signal, boolean useGroundShieldRate, CallbackInfoReturnable<Boolean> cir) {
        this.sphereshields$poweredByEnergy = cir.getReturnValue();
    }

    // Same story as the signal field in StaticSphereMixin: only ever set server-side, but
    // shieldColor() also runs on the client - needs NBT sync to actually reach it.
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void sphereshields$saveEnergyFlag(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean(POWERED_BY_ENERGY_KEY, this.sphereshields$poweredByEnergy);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void sphereshields$loadEnergyFlag(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.sphereshields$poweredByEnergy = tag.getBoolean(POWERED_BY_ENERGY_KEY);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void sphereshields$includeEnergyFlagInUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean(POWERED_BY_ENERGY_KEY, this.sphereshields$poweredByEnergy);
    }

    @Inject(method = "shieldColor", at = @At("HEAD"), cancellable = true)
    private void sphereshields$tieredColor(CallbackInfoReturnable<Integer> cir) {
        ShieldGeneratorBlockEntityAccessor self = (ShieldGeneratorBlockEntityAccessor) (Object) this;
        if (!self.sphereshields$isGroundShieldMode()) return;
        cir.setReturnValue(sphereshields$currentStop(self).color());
    }

    @Inject(method = "shieldStrength", at = @At("HEAD"), cancellable = true)
    private void sphereshields$tieredStrength(CallbackInfoReturnable<Double> cir) {
        ShieldGeneratorBlockEntityAccessor self = (ShieldGeneratorBlockEntityAccessor) (Object) this;
        if (!self.sphereshields$isGroundShieldMode()) return;
        cir.setReturnValue(sphereshields$currentStop(self).strength());
    }

    @Unique
    private Stop sphereshields$currentStop(ShieldGeneratorBlockEntityAccessor self) {
        int signal = ((SignalHolder) (Object) this).sphereshields$getLastSignal();
        double coefficient = (signal / 15.0) * (signal / 15.0) * MAX_COEFFICIENT;

        if (this.sphereshields$poweredByEnergy) {
            return sphereshields$interpolate(coefficient, ENERGY_PATH);
        }

        int fuelTier = self.sphereshields$getFuelTier();
        return switch (fuelTier) {
            case 4 -> new Stop(coefficient, AMETHYST_COLOR, AMETHYST_STRENGTH); // amethyst: always top tier
            case 3 -> sphereshields$interpolate(coefficient, QUARTZ_PATH);
            case 2 -> sphereshields$interpolate(coefficient, LAPIS_PATH);
            default -> new Stop(coefficient, COPPER_COLOR, COPPER_STRENGTH); // copper (or no fuel data yet): always bottom tier
        };
    }

    @Unique
    private static Stop sphereshields$interpolate(double coefficient, Stop[] path) {
        if (coefficient <= path[0].coefficient()) return path[0];
        for (int i = 0; i < path.length - 1; i++) {
            Stop a = path[i];
            Stop b = path[i + 1];
            if (coefficient <= b.coefficient()) {
                double t = (coefficient - a.coefficient()) / (b.coefficient() - a.coefficient());
                return new Stop(coefficient, blend(a.color(), b.color(), t), lerp(a.strength(), b.strength(), t));
            }
        }
        return path[path.length - 1];
    }

    @Unique
    private static int blend(int colorA, int colorB, double t) {
        t = Math.clamp(t, 0.0, 1.0);
        int ar = (colorA >> 16) & 0xFF, ag = (colorA >> 8) & 0xFF, ab = colorA & 0xFF;
        int br = (colorB >> 16) & 0xFF, bg = (colorB >> 8) & 0xFF, bb = colorB & 0xFF;
        int r = (int) Math.round(ar + (br - ar) * t);
        int g = (int) Math.round(ag + (bg - ag) * t);
        int b = (int) Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | b;
    }

    @Unique
    private static double lerp(double a, double b, double t) {
        t = Math.clamp(t, 0.0, 1.0);
        return a + (b - a) * t;
    }
}
