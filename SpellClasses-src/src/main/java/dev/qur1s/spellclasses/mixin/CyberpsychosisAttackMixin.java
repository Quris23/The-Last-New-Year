package dev.qur1s.spellclasses.mixin;

import com.chromeware.humanity.HumanityPackets;
import dev.qur1s.spellclasses.implant.CyberpsychosisSymptoms;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every cyberpsychosis attack (the "episode" packet of kind 1 that Chromeware sends to the player) rolls our extra symptoms. */
@Pseudo
@Mixin(value = HumanityPackets.class, remap = false)
public abstract class CyberpsychosisAttackMixin {
    @Inject(method = "send(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
            at = @At("HEAD"), remap = false, require = 0)
    private static void spellclasses$attack(ServerPlayer player, CustomPacketPayload payload, CallbackInfo ci) {
        if (payload instanceof HumanityPackets.Episode episode && episode.kind() == 1) CyberpsychosisSymptoms.onAttack(player);
    }
}
