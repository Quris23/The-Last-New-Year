package dev.qur1s.spellclasses.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FTB Quests' S2C payloads (task progress, quest/reward notifications, ...) go through
 * Architectury's networking bridge, which on this NeoForge/architectury/ftbquests combination has
 * a genuine, reproducible bug: encoding several of these payloads throws ClassCastException
 * (e.g. UpdateTaskProgressMessage cannot be cast to NetworkAggregator$BufCustomPacketPayload) -
 * confirmed independent of which architectury/ftbquests version is installed (tried multiple
 * combinations, including redirecting the send call to a plain vanilla packet - same crash, so the
 * registered dispatch codec for these payload ids is itself wrapped inconsistently, not just the
 * call site). Vanilla's own exceptionCaught unconditionally disconnects the player on any encode
 * failure, which for these packets means losing connection over what is, at worst, one missed
 * client-side toast/live update.
 * <p>
 * FTB Quests already updates its own server-side team/progress data (and our own
 * TeamDataPersonalProgressMixin/PersonalQuestProgress for class-gated chapters) before attempting
 * this broadcast, so the safe fix is the same one vanilla already uses for SkipPacketException:
 * swallow it and keep the connection alive instead of disconnecting. Reopening the quest book
 * re-syncs everything correctly regardless.
 */
@Mixin(value = Connection.class, remap = false)
public abstract class FtbQuestsEncoderCrashFixMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpellClasses/FtbQuestsNetworkFix");

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void spellclasses$suppressFtbQuestsEncodeCrash(ChannelHandlerContext context, Throwable exception, CallbackInfo ci) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains("dev.ftb.mods.ftbquests") && message.contains("NetworkAggregator")) {
                LOGGER.warn("Suppressed a known FTB Quests/Architectury packet-encode bug instead of disconnecting: {}", message);
                ci.cancel();
                return;
            }
            cause = cause.getCause();
        }
    }
}
