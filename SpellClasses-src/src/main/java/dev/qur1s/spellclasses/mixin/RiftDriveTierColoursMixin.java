package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveBlockEntity;

/**
 * AeroWarptics' per-tier fallback colours (used for the warp-corridor flight effect and the Drive
 * block's own glow when no Rift Modulator is linked) live only as five int literals baked into
 * {@code RiftDriveBlockEntity}'s static initializer for {@code TIER_COLOURS[]} - teal/cyan/gold/pink/
 * white by default. By user design, replaced with a green-to-dark-green gradient by tier, matching the
 * same "standard Green/Dark Green" scheme as {@link RiftModulatorDefaultColourMixin} and the recolored
 * portal/fluid textures.
 */
@Mixin(value = RiftDriveBlockEntity.class, remap = false)
public abstract class RiftDriveTierColoursMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 3123384))
    private static int spellclasses$tier1Colour(int original) {
        return 0x55FF7A;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 4839876))
    private static int spellclasses$tier2Colour(int original) {
        return 0x2ECC40;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 14725194))
    private static int spellclasses$tier3Colour(int original) {
        return 0x1F9E33;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 14966015))
    private static int spellclasses$singularityColour(int original) {
        return 0x0F5C1C;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 16777215))
    private static int spellclasses$creativeColour(int original) {
        return 0x063311;
    }
}
