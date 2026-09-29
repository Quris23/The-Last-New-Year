package dev.qur1s.spellclasses.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.StatTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.XPTask;
import dev.qur1s.spellclasses.ClassManager;
import dev.qur1s.spellclasses.FtbQuestsClassGate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-player gate for a class-gated chapter's tasks: even though a teammate with a different class
 * can see the chapter (see {@link FtbQuestsChapterVisibilityMixin}), only a player who actually has
 * the matching class can submit its tasks.
 * <p>
 * Originally this only targeted the {@code final submitTask(TeamData, ServerPlayer)} overload - but
 * that one is just a thin wrapper calling {@code submitTask(TeamData, ServerPlayer, ItemStack)} with
 * an empty stack, and that 3-arg overload is NOT final: {@code ItemTask}, {@code StatTask},
 * {@code XPTask}, {@code AbstractBooleanTask} (covers every simple boolean-style task -
 * {@code CheckmarkTask}, {@code StageTask}, {@code LocationTask}, {@code AdvancementTask},
 * {@code StructureTask}, {@code DimensionTask}, {@code BiomeTask}, {@code ObservationTask}) and
 * {@code CustomTask} all override it directly with their own logic, bypassing the base class's
 * (guarded) implementation entirely via virtual dispatch. Item-collection tasks in particular call
 * their own override with the real submitted stack, never touching the 2-arg wrapper at all - which
 * is exactly how a player with the wrong class could still turn in another class's item task despite
 * not being able to see/complete it through the intended flow. Targeting all six classes here (Mixin
 * applies the same injection to every listed target) closes that off: {@code KillTask},
 * {@code FluidTask} and {@code EnergyTask}/{@code ForgeEnergyTask} don't override the 3-arg method at
 * all, so they're still covered by {@code Task}'s own base implementation being targeted directly.
 */
@Mixin(value = {Task.class, ItemTask.class, StatTask.class, XPTask.class, AbstractBooleanTask.class, CustomTask.class}, remap = false)
public abstract class FtbQuestsTaskGateMixin {
    @Inject(method = "submitTask(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void spellclasses$blockWrongClassSubmit(TeamData teamData, ServerPlayer player, ItemStack stack, CallbackInfo ci) {
        ResourceLocation requiredSchool = FtbQuestsClassGate.requiredSchoolFor(((Task) (Object) this).getQuestChapter().getId());
        if (requiredSchool == null) return;
        boolean hasClass = ClassManager.chosenSchool(player).map(requiredSchool::equals).orElse(false);
        if (!hasClass) {
            ci.cancel();
        }
    }
}
