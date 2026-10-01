package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import uk.co.iceconchy.aerowarptics.modulator.RiftModulatorBlockEntity;

/**
 * AeroWarptics bakes its {@code RiftModulatorBlockEntity.DEFAULT_COLOUR} (0x9B6DFF, purple) straight
 * into both the constructor's field initializers and {@code read()}'s NBT-missing fallback as compile-
 * time integer literals - the constant field itself is never referenced at either site (javac inlines
 * it), so a Mixin on the field wouldn't change anything actually read at runtime. Retargeting the
 * literal {@code 10185727} wherever it appears is the only way to change the default. By user design,
 * this is "standard" Green (colour) / Dark Green (accentColour) instead - matching the green portal/
 * fluid texture recolor in kubejs/assets/aerowarptics. Un-modulated Rift Gates (no linked Modulator) and
 * freshly-placed Modulators then default to this scheme instead of purple.
 */
@Mixin(value = RiftModulatorBlockEntity.class, remap = false)
public abstract class RiftModulatorDefaultColourMixin {
    private static final int GREEN = 0x2ECC40;
    private static final int DARK_GREEN = 0x145C1E;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 10185727, ordinal = 0))
    private int spellclasses$defaultColour(int original) {
        return GREEN;
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 10185727, ordinal = 1))
    private int spellclasses$defaultAccentColour(int original) {
        return DARK_GREEN;
    }

    @ModifyConstant(method = "read", constant = @Constant(intValue = 10185727))
    private int spellclasses$defaultColourOnLoad(int original) {
        return GREEN;
    }
}
