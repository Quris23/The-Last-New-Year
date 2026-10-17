package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.UpdateTaskProgressMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import dev.qur1s.spellclasses.data.PersonalQuestProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Every player's progress on the class-gated ("Магия") chapters is tracked in their own private
 * {@link dev.qur1s.spellclasses.data.PersonalQuestData} attachment instead of the shared party
 * {@link TeamData}, so one player finishing "Ледяной Маг" doesn't finish it for teammates - even
 * though everyone is in the same FTB Team. Everything outside those chapters (the Lore chapter
 * etc.) is untouched and stays fully shared, as before.
 * <p>
 * Player resolution: server-side, {@link ServerQuestFile#getCurrentPlayer()} - FTB Quests itself
 * already wraps every task submission and auto-submit tick in {@code withPlayerContext}, so this
 * is always the player whose action triggered the read/write. Client-side, there's never any
 * ambiguity - a client only ever renders its own book - so it's simply the local player.
 */
@Mixin(value = TeamData.class, remap = false)
public abstract class TeamDataPersonalProgressMixin {

    /** Vanilla only clears the shared {@code taskProgress} map, which class-gated tasks don't read from. */
    @Inject(method = "resetProgress", at = @At("HEAD"), cancellable = true)
    private void spellclasses$resetProgress(Task task, CallbackInfo ci) {
        if (!FtbQuestsClassGate.isPersonalObject(task)) return;
        ci.cancel();
        Player player = spellclasses$resolvePlayer((TeamData) (Object) this);
        if (player == null) return;
        PersonalQuestProgress.setProgress(player, task.id, 0L);
        PersonalQuestProgress.setStarted(player, task.id, false);
        PersonalQuestProgress.setCompleted(player, task.id, false);
        TeamData reset = (TeamData) (Object) this;
        spellclasses$share(reset, task, task.id, 0L, Boolean.FALSE, Boolean.FALSE);
    }

    @Inject(method = "getProgress(Ldev/ftb/mods/ftbquests/quest/task/Task;)J", at = @At("HEAD"), cancellable = true)
    private void spellclasses$getProgress(Task task, CallbackInfoReturnable<Long> cir) {
        if (!FtbQuestsClassGate.isPersonalObject(task)) return;
        Player player = spellclasses$resolvePlayer((TeamData) (Object) this);
        if (player != null) {
            cir.setReturnValue(PersonalQuestProgress.getProgress(player, task.id));
        }
    }

    @Inject(method = "isStarted", at = @At("HEAD"), cancellable = true)
    private void spellclasses$isStarted(QuestObject object, CallbackInfoReturnable<Boolean> cir) {
        if (!FtbQuestsClassGate.isPersonalObject(object)) return;
        Player player = spellclasses$resolvePlayer((TeamData) (Object) this);
        if (player != null) {
            cir.setReturnValue(PersonalQuestProgress.isStarted(player, object.id));
        }
    }

    @Inject(method = "isCompleted", at = @At("HEAD"), cancellable = true)
    private void spellclasses$isCompleted(QuestObject object, CallbackInfoReturnable<Boolean> cir) {
        if (!FtbQuestsClassGate.isPersonalObject(object)) return;
        Player player = spellclasses$resolvePlayer((TeamData) (Object) this);
        if (player != null) {
            cir.setReturnValue(PersonalQuestProgress.isCompleted(player, object.id));
        }
    }

    /** Bypasses TeamData's per-quest-id dependency cache, which has no idea progress is now per-player. */
    @Inject(method = "areDependenciesComplete", at = @At("HEAD"), cancellable = true)
    private void spellclasses$depsComplete(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        if (!FtbQuestsClassGate.isPersonalChapter(quest.getChapter().getId())) return;
        cir.setReturnValue(quest.areDependenciesComplete((TeamData) (Object) this));
    }

    @Inject(method = "areDependenciesVisible", at = @At("HEAD"), cancellable = true)
    private void spellclasses$depsVisible(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        if (!FtbQuestsClassGate.isPersonalChapter(quest.getChapter().getId())) return;
        cir.setReturnValue(quest.areDependenciesVisible((TeamData) (Object) this));
    }

    @Inject(method = "setProgress", at = @At("HEAD"), cancellable = true)
    private void spellclasses$setProgress(Task task, long progress, CallbackInfo ci) {
        if (!FtbQuestsClassGate.isPersonalObject(task)) return;
        ci.cancel();
        TeamData self = (TeamData) (Object) this;
        if (self.isLocked()) return;
        Player player = spellclasses$resolvePlayer(self);
        if (player == null) return;
        // Progress on a class chapter's tasks (kills, items, stats, ...) only counts for a player of that class.
        net.minecraft.resources.ResourceLocation requiredSchool = FtbQuestsClassGate.requiredSchoolForObject(task);
        if (requiredSchool != null && progress > 0L && player instanceof ServerPlayer classPlayer
                && !dev.qur1s.spellclasses.ClassManager.chosenSchool(classPlayer).map(requiredSchool::equals).orElse(false)) {
            return;
        }

        long maxProgress = task.getMaxProgress();
        long clamped = Math.max(0L, Math.min(progress, maxProgress));
        long prev = PersonalQuestProgress.getProgress(player, task.id);
        boolean prevStarted = PersonalQuestProgress.isStarted(player, task.id);
        if (prev == clamped && !(clamped == 0L && prevStarted)) return;

        PersonalQuestProgress.setProgress(player, task.id, clamped);
        spellclasses$share(self, task, task.id, clamped, null, null);
        if (!self.getFile().isServerSide() || !(player instanceof ServerPlayer serverPlayer)) return;

        PacketDistributor.sendToPlayer(serverPlayer, new UpdateTaskProgressMessage(self.getTeamId(), task.id, clamped));
        if (prev == 0L) {
            task.onStarted(new QuestProgressEventData<>(new Date(), self, task, List.of(serverPlayer), Collections.emptyList()));
        }
        if (clamped >= maxProgress && self.areDependenciesComplete(task.getQuest())) {
            spellclasses$completePersonally(self, task, serverPlayer);
        }
    }

    @Inject(method = "setStarted", at = @At("HEAD"), cancellable = true)
    private void spellclasses$setStarted(long id, Date time, CallbackInfoReturnable<Boolean> cir) {
        TeamData self = (TeamData) (Object) this;
        QuestObject object = self.getFile().get(id);
        if (object == null || !FtbQuestsClassGate.isPersonalObject(object)) return;
        Player player = spellclasses$resolvePlayer(self);
        if (player == null) {
            cir.setReturnValue(false);
            return;
        }
        boolean was = PersonalQuestProgress.isStarted(player, id);
        boolean now = time != null;
        PersonalQuestProgress.setStarted(player, id, now);
        spellclasses$share(self, object, id, -1L, now, null);
        cir.setReturnValue(was != now);
    }

    @Inject(method = "setCompleted", at = @At("HEAD"), cancellable = true)
    private void spellclasses$setCompleted(long id, Date time, CallbackInfoReturnable<Boolean> cir) {
        TeamData self = (TeamData) (Object) this;
        QuestObject object = self.getFile().get(id);
        if (object == null || !FtbQuestsClassGate.isPersonalObject(object)) return;
        Player player = spellclasses$resolvePlayer(self);
        if (player == null) {
            cir.setReturnValue(false);
            return;
        }
        boolean was = PersonalQuestProgress.isCompleted(player, id);
        boolean now = time != null;
        PersonalQuestProgress.setCompleted(player, id, now);
        spellclasses$share(self, object, id, -1L, null, now);
        if (!now && object instanceof Quest resetQuest && FtbQuestsClassGate.isSharedObject(object)) {
            // a quest taken back (progress reset): its tasks go back too, otherwise they stay "completed" and it can't be earned again
            for (Task task : resetQuest.getTasksAsList()) {
                PersonalQuestProgress.setProgress(player, task.id, 0L);
                PersonalQuestProgress.setStarted(player, task.id, false);
                PersonalQuestProgress.setCompleted(player, task.id, false);
                spellclasses$share(self, task, task.id, 0L, Boolean.FALSE, Boolean.FALSE);
            }
        }
        if (now && !was && object instanceof Quest && FtbQuestsClassGate.isSharedObject(object)
                && self.getFile().isServerSide() && player instanceof ServerPlayer completer
                && net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
            dev.qur1s.spellclasses.data.SharedQuestStore.shareCompleter(
                    net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer(), id, completer.getGameProfile().getName());
        }
        cir.setReturnValue(was != now);
    }

    /**
     * Only reached if something calls this directly rather than through our own
     * {@link #spellclasses$setProgress} (which completes tasks itself, scoped to one player).
     */
    @Inject(method = "markTaskCompleted", at = @At("HEAD"), cancellable = true)
    private void spellclasses$markTaskCompleted(Task task, CallbackInfo ci) {
        if (!FtbQuestsClassGate.isPersonalObject(task)) return;
        ci.cancel();
        TeamData self = (TeamData) (Object) this;
        if (!self.getFile().isServerSide()) return;
        Player player = spellclasses$resolvePlayer(self);
        if (player instanceof ServerPlayer serverPlayer) {
            spellclasses$completePersonally(self, task, serverPlayer);
        }
    }

    /** For the shared "История" chapter only: mirror a change to every player and to the world-wide store (server side). */
    private static void spellclasses$share(TeamData self, QuestObject object, long id, long progress, Boolean started, Boolean completed) {
        if (!self.getFile().isServerSide() || !FtbQuestsClassGate.isSharedObject(object)) return;
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (progress >= 0L) dev.qur1s.spellclasses.data.SharedQuestStore.shareProgress(server, id, progress);
        if (started != null) dev.qur1s.spellclasses.data.SharedQuestStore.shareStarted(server, id, started);
        if (completed != null) dev.qur1s.spellclasses.data.SharedQuestStore.shareCompleted(server, id, completed);
    }

    private static void spellclasses$completePersonally(TeamData self, Task task, ServerPlayer player) {
        List<ServerPlayer> justThisPlayer = List.of(player);
        task.onCompleted(new QuestProgressEventData<>(new Date(), self, task, justThisPlayer, justThisPlayer));
    }

    /**
     * Not every FTB Quests code path wraps its work in {@code withPlayerContext} - notably
     * {@code FTBQuestsEventHandler.playerKill} (kill tasks) never sets it, so
     * {@code getCurrentPlayer()} comes back null there even though the killer is perfectly well
     * known. Falls back to this team's one online member, which is exactly correct for the
     * solo (one-player-per-team) setup this pack actually runs.
     */
    private static Player spellclasses$resolvePlayer(TeamData self) {
        if (self.getFile().isServerSide()) {
            Player current = ServerQuestFile.INSTANCE.getCurrentPlayer();
            if (current != null) return current;
            return self.getOnlineMembers().stream().findFirst().orElse(null);
        }
        return Minecraft.getInstance().player;
    }
}
