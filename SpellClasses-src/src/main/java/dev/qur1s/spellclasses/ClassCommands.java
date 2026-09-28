package dev.qur1s.spellclasses;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.qur1s.spellclasses.data.ClassAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashSet;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class ClassCommands {
    private ClassCommands() {
    }

    private static final SuggestionProvider<CommandSourceStack> SCHOOL_SUGGESTIONS = (ctx, builder) -> {
        for (ResourceLocation school : ClassSchools.restrictedSchools()) {
            builder.suggest(school.getPath());
        }
        return builder.buildFuture();
    };

    @SubscribeEvent
    static void onRegister(RegisterCommandsEvent event) {
        var bookNode = Commands.literal("book")
                .executes(ctx -> giveBook(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> giveBook(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));

        var chooseNode = Commands.literal("choose")
                .then(Commands.argument("school", StringArgumentType.word())
                        .suggests(SCHOOL_SUGGESTIONS)
                        .executes(ctx -> chooseSchool(ctx.getSource(), ctx.getSource().getPlayerOrException(),
                                StringArgumentType.getString(ctx, "school")))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> chooseSchool(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                        StringArgumentType.getString(ctx, "school")))));

        var resetUniversalGateNode = Commands.literal("reset_universal_gate")
                .executes(ctx -> resetUniversalGate(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> resetUniversalGate(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));

        event.getDispatcher().register(Commands.literal("spellclasses")
                .requires(source -> source.hasPermission(2))
                .then(bookNode)
                .then(chooseNode)
                .then(resetUniversalGateNode));
    }

    /** Debug/testing: clears the "ever obtained this universal-chapter item" flag - see FtbQuestsUniversalGate. */
    private static int resetUniversalGate(CommandSourceStack source, ServerPlayer target) {
        target.setData(ClassAttachments.OBTAINED_UNIVERSAL_ITEMS, new HashSet<>());
        source.sendSuccess(() -> Component.literal("Cleared " + target.getName().getString()
                + "'s universal chapter unlock flags (Abyssal/Sand)."), true);
        return 1;
    }

    private static int giveBook(CommandSourceStack source, ServerPlayer target) {
        ClassManager.reissueClassBook(target);
        source.sendSuccess(() -> Component.literal("Gave " + target.getName().getString() + " a fresh class book (their class was reset)."), true);
        return 1;
    }

    private static int chooseSchool(CommandSourceStack source, ServerPlayer target, String schoolName) {
        ResourceLocation school = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", schoolName);
        if (!ClassSchools.restrictedSchools().contains(school)) {
            source.sendFailure(Component.literal("Unknown class: " + schoolName)
                    .copy().withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!ClassManager.forceChooseSchool(target, school)) {
            source.sendFailure(Component.literal("That class is full.").withStyle(ChatFormatting.RED));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " is now a " + schoolName + "."), true);
        return 1;
    }
}
