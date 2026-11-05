package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The Legs group of the Chromeware menu shows three sockets instead of one (the sockets themselves are raised in LegSockets). */
@Pseudo
@Mixin(targets = "com.chromeware.client.cyberware.CyberwareMenuLayout", remap = false)
public abstract class CyberwareMenuLayoutMixin {
    @ModifyArg(method = "<clinit>", remap = false, require = 0, index = 1,
            at = @At(value = "INVOKE", ordinal = 4, remap = false,
                    target = "Lcom/chromeware/client/cyberware/CyberwareMenuLayout;right(Ljava/lang/String;III)Lcom/chromeware/client/cyberware/CyberwareMenuLayout$Group;"))
    private static int spellclasses$threeLegSockets(int count) {
        return count == 1 ? 3 : count;
    }

    /** The Operating System group (the first one on the right) gets two more sockets, the К ones. */
    @ModifyArg(method = "<clinit>", remap = false, require = 0, index = 1,
            at = @At(value = "INVOKE", ordinal = 0, remap = false,
                    target = "Lcom/chromeware/client/cyberware/CyberwareMenuLayout;right(Ljava/lang/String;III)Lcom/chromeware/client/cyberware/CyberwareMenuLayout$Group;"))
    private static int spellclasses$threeSystemSockets(int count) {
        return count == 1 ? 3 : count;
    }

    /** The Face group (the second one on the right) gets a third socket. */
    @ModifyArg(method = "<clinit>", remap = false, require = 0, index = 1,
            at = @At(value = "INVOKE", ordinal = 1, remap = false,
                    target = "Lcom/chromeware/client/cyberware/CyberwareMenuLayout;right(Ljava/lang/String;III)Lcom/chromeware/client/cyberware/CyberwareMenuLayout$Group;"))
    private static int spellclasses$threeFaceSockets(int count) {
        return count == 2 ? 3 : count;
    }
}
