package dev.qur1s.spellclasses.mixin;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable throws "Cannot drop chunks in plot" when the server tells the client to forget a ship-plot chunk, and
 * vanilla turns an exception in a non-skippable packet into a disconnect. That happens when a player is
 * moved out of the plot area by hand (/tp away from the ~20,000,000 block coordinates of a seat, logging in
 * while still sitting in a seat, ...). Plot chunks are managed by Sable itself, so just ignore the packet.
 */
@Mixin(value = ClientPacketListener.class, remap = false)
public abstract class SablePlotChunkForgetMixin {
    @Inject(method = "handleForgetLevelChunk", at = @At("HEAD"), cancellable = true)
    private void spellclasses$ignorePlotChunkForget(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        ClientLevel level = ((ClientPacketListener) (Object) this).getLevel();
        SubLevelContainer container = level == null ? null : SubLevelContainer.getContainer(level);
        if (container != null && container.inBounds(packet.pos())) {
            ci.cancel();
        }
    }
}
