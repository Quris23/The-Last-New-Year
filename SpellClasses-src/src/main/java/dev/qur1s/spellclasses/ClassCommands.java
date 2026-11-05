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

        var spawnNode = Commands.literal("spawn")
                .then(Commands.literal("set").then(Commands.argument("school", StringArgumentType.word())
                        .suggests(SCHOOL_SUGGESTIONS)
                        .executes(ctx -> setSpawn(ctx.getSource(), StringArgumentType.getString(ctx, "school")))))
                .then(Commands.literal("clear").then(Commands.argument("school", StringArgumentType.word())
                        .suggests(SCHOOL_SUGGESTIONS)
                        .executes(ctx -> clearSpawn(ctx.getSource(), StringArgumentType.getString(ctx, "school")))))
                .then(Commands.literal("info").then(Commands.argument("school", StringArgumentType.word())
                        .suggests(SCHOOL_SUGGESTIONS)
                        .executes(ctx -> spawnInfo(ctx.getSource(), StringArgumentType.getString(ctx, "school")))))
                .then(Commands.literal("tp").then(Commands.argument("school", StringArgumentType.word())
                        .suggests(SCHOOL_SUGGESTIONS)
                        .executes(ctx -> tpSpawn(ctx.getSource(), StringArgumentType.getString(ctx, "school")))));

        var structureNode = Commands.literal("structure")
                .then(Commands.literal("place")
                        .then(Commands.literal("abandoned_observatory").executes(ctx -> placeObservatory(ctx.getSource())))
                        .then(Commands.literal("ender").executes(ctx -> placeObservatory(ctx.getSource()))));

        var compassNode = Commands.literal("compass")
                .executes(ctx -> giveCompass(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> giveCompass(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));

        event.getDispatcher().register(Commands.literal("spellclasses")
                .requires(source -> source.hasPermission(2))
                .then(bookNode)
                .then(chooseNode)
                .then(spawnNode)
                .then(structureNode)
                .then(compassNode)
                .then(resetUniversalGateNode));
    }

    /** Builds the Abandoned Observatory (the Ender class's place) around the command player and makes it Ender's spawn. */
    private static int placeObservatory(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var level = source.getServer().overworld();
        if (player.level() != level) {
            source.sendFailure(Component.literal("Stand in the overworld: class spawns are there."));
            return 0;
        }
        net.minecraft.core.BlockPos corner = ClassSpawns.observatoryCorner(player.blockPosition());
        if (!ClassSpawns.place(level, corner)) {
            source.sendFailure(Component.literal("The structure file spellclasses:abandoned_observatory is missing."));
            return 0;
        }
        net.minecraft.core.BlockPos spawn = ClassSpawns.observatorySpawn(corner);
        dev.qur1s.spellclasses.data.ClassSpawnData.get(level)
                .set(io.redspace.ironsspellbooks.api.registry.SchoolRegistry.ENDER_RESOURCE.toString(), spawn);
        source.sendSuccess(() -> Component.literal("Abandoned Observatory built at " + corner.toShortString()
                + "; the Ender class spawns at " + spawn.toShortString() + "."), true);
        return 1;
    }

    private static int giveCompass(CommandSourceStack source, ServerPlayer target) {
        if (!ClassManager.giveObservatoryCompass(target)) {
            source.sendFailure(Component.literal("The Abandoned Observatory has no place yet (no level ground found)."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Gave " + target.getName().getString() + " a compass to the Abandoned Observatory."), true);
        return 1;
    }

    private static ResourceLocation school(String name) {
        for (ResourceLocation school : ClassSchools.restrictedSchools()) {
            if (school.getPath().equals(name)) return school;
        }
        return null;
    }

    /** Sets the class spawn to where the command's player stands (for Ender: the hand-built structure). */
    private static int setSpawn(CommandSourceStack source, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation school = school(name);
        if (school == null) { source.sendFailure(Component.literal("Unknown class: " + name)); return 0; }
        ServerPlayer player = source.getPlayerOrException();
        if (player.level() != source.getServer().overworld()) {
            source.sendFailure(Component.literal("Class spawns are in the overworld: stand there first."));
            return 0;
        }
        net.minecraft.core.BlockPos pos = player.blockPosition();
        dev.qur1s.spellclasses.data.ClassSpawnData.get(source.getServer().overworld()).set(school.toString(), pos);
        source.sendSuccess(() -> Component.literal("Spawn of " + name + " set to " + pos.toShortString()), true);
        return 1;
    }

    private static int clearSpawn(CommandSourceStack source, String name) {
        ResourceLocation school = school(name);
        if (school == null) { source.sendFailure(Component.literal("Unknown class: " + name)); return 0; }
        boolean removed = dev.qur1s.spellclasses.data.ClassSpawnData.get(source.getServer().overworld()).clear(school.toString());
        source.sendSuccess(() -> Component.literal(removed ? "Spawn of " + name + " forgotten: it is searched for again next time."
                : "Spawn of " + name + " was not set."), true);
        return 1;
    }

    private static int spawnInfo(CommandSourceStack source, String name) {
        ResourceLocation school = school(name);
        if (school == null) { source.sendFailure(Component.literal("Unknown class: " + name)); return 0; }
        var pos = dev.qur1s.spellclasses.data.ClassSpawnData.get(source.getServer().overworld()).get(school.toString());
        source.sendSuccess(() -> Component.literal(pos.map(p -> "Spawn of " + name + ": " + p.toShortString())
                .orElse("Spawn of " + name + " is not found yet (it is searched for when the first player chooses it).")), false);
        return 1;
    }

    private static int tpSpawn(CommandSourceStack source, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation school = school(name);
        if (school == null) { source.sendFailure(Component.literal("Unknown class: " + name)); return 0; }
        ClassSpawns.teleport(source.getPlayerOrException(), school);
        return 1;
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
        ResourceLocation school = school(schoolName);
        if (school == null) {
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
