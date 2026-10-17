package dev.qur1s.reviveraltar.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;

/** /revivealtar revive <player> - forces a revival, bypassing the ritual altar. Requires op level 2. */
public class RevivalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("revivealtar")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("revive")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(RevivalCommand::reviveCommand))));
    }

    private static int reviveCommand(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");

        int successCount = 0;
        for (GameProfile profile : profiles) {
            boolean success = RevivalManager.forceRevive(level, profile.getId(), profile.getName());
            if (success) {
                successCount++;
            } else {
                source.sendFailure(Component.translatable("commands.reviveraltar.not_a_ghost", profile.getName()));
            }
        }

        int finalCount = successCount;
        if (finalCount > 0) {
            source.sendSuccess(() -> Component.translatable("commands.reviveraltar.revive_success", finalCount), true);
        }
        return successCount;
    }
}
