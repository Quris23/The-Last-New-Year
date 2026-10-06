package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveBlockEntity;

/**
 * AeroWarptics' per-tier fallback colours (used for the warp-corridor flight effect and the Drive
 * block's own glow when no Rift Modulator is linked) live only as five int literals baked into
 * {@code RiftDriveBlockEntity}'s static initializer for {@code TIER_COLOURS[]} - teal/cyan/gold/pink/
 * white by default. By user design, replaced with a purple gradient by tier (light to deep purple),
 * matching the Rift Modulator's own default purple (0x9B6DFF) that is left untouched.
 */
@Mixin(value = RiftDriveBlockEntity.class, remap = false)
public abstract class RiftDriveTierColoursMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 3123384))
    private static int spellclasses$tier1Colour(int original) {
        return 0xC084FC;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 4839876))
    private static int spellclasses$tier2Colour(int original) {
        return 0xA855F7;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 14725194))
    private static int spellclasses$tier3Colour(int original) {
        return 0x8B3FD9;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 14966015))
    private static int spellclasses$singularityColour(int original) {
        return 0x6D28D9;
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 16777215))
    private static int spellclasses$creativeColour(int original) {
        return 0x4C1D95;
    }
}
