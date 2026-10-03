package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.NetherGate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.co.iceconchy.aerowarptics.gate.RiftGate;
import uk.co.iceconchy.aerowarptics.gate.RiftGateRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A Rift Gate's dial list used to show only gates in its own dimension. Now it shows every visible gate, plus the
 * built-in "Ад" entry (until the real Nether gate exists) when dialling from anywhere but the Nether.
 */
@Mixin(value = RiftGateRegistry.class, remap = false)
public abstract class RiftGateRegistryCrossMixin {
    @Shadow
    public abstract List<RiftGate> filtered(Predicate<RiftGate> predicate);

    @Inject(method = "dialableFrom", at = @At("HEAD"), cancellable = true)
    private void spellclasses$everyDimension(Player player, ResourceKey<Level> dimension, @Nullable UUID excluding,
                                             CallbackInfoReturnable<List<RiftGate>> cir) {
        List<RiftGate> list = new ArrayList<>(this.filtered(
                gate -> gate.enabled() && gate.isVisibleTo(player) && !gate.id().equals(excluding)));
        boolean haveNether = list.stream().anyMatch(gate -> gate.id().equals(NetherGate.ID));
        if (!haveNether && !Level.NETHER.equals(dimension) && !NetherGate.ID.equals(excluding)) {
            list.add(NetherGate.placeholder());
        }
        cir.setReturnValue(list);
    }
}
