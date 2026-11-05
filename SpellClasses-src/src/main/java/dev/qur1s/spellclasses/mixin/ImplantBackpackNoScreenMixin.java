package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.ImplantBackpack;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The hidden implant backpack has no screen: the "open backpack" key never opens it (neither by name nor as the first backpack found). */
@Pseudo
@Mixin(value = BackpackOpenPayload.class, remap = false)
public abstract class ImplantBackpackNoScreenMixin {
    @Inject(method = "handlePayload", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$byName(BackpackOpenPayload payload, IPayloadContext context, CallbackInfo ci) {
        if (ImplantBackpack.HANDLER.equals(payload.handlerName())) ci.cancel();
    }

    @ModifyArg(method = "findAndOpenFirstBackpack", remap = false, require = 0, index = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/p3pp3rf1y/sophisticatedbackpacks/util/PlayerInventoryProvider;runOnBackpacks(Lnet/minecraft/world/entity/player/Player;Lnet/p3pp3rf1y/sophisticatedbackpacks/util/PlayerInventoryProvider$BackpackInventorySlotConsumer;)Z"))
    private static PlayerInventoryProvider.BackpackInventorySlotConsumer spellclasses$skipHidden(
            PlayerInventoryProvider.BackpackInventorySlotConsumer original) {
        return (backpack, handler, identifier, slot) -> !ImplantBackpack.HANDLER.equals(handler)
                && original.accept(backpack, handler, identifier, slot);
    }
}
