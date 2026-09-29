package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code useWithoutItem} is the base mod's GUI-open handler - it runs on right-click regardless of
 * the held item (that's the whole point of the name), which is exactly why it needs gating here:
 * by user design, the shield generator's settings screen should only open while holding the
 * Artificer's Cane ({@code irons_spellbooks:artificer_cane}, "Трость изобретателя"). Matched by
 * registry id rather than a compile dependency on irons_spellbooks, since this mod doesn't otherwise
 * need it. Cancelling back to PASS (rather than consuming the interaction) lets whatever's actually
 * in hand fall through to its own normal use behaviour instead of just eating the click.
 */
@Mixin(value = ShieldGeneratorBlock.class, remap = false)
abstract class ArtificerCaneGateMixin {
    private static final ResourceLocation ARTIFICER_CANE = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "artificer_cane");

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void sphereshields$requireArtificerCane(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!sphereshields$isArtificerCane(player.getMainHandItem()) && !sphereshields$isArtificerCane(player.getOffhandItem())) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Unique
    private static boolean sphereshields$isArtificerCane(ItemStack stack) {
        return !stack.isEmpty() && ARTIFICER_CANE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
