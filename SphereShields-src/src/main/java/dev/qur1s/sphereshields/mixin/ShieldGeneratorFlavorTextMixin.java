package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Renames the shield generator's item/block display and adds a flavor-text tooltip describing its
 * fuel tiers and the redstone requirement, by user design. Targets the vanilla {@code Item} methods
 * directly rather than a lang override, since {@code BlockItem} never overrides either one itself -
 * the base {@code Item} implementation is what actually runs for it, so injecting there is guaranteed
 * to fire regardless of resource pack load order (a plain lang-file override of shield_generators'
 * own translation key would depend on unpredictable mod resource-pack ordering).
 */
@Mixin(value = Item.class, remap = false)
abstract class ShieldGeneratorFlavorTextMixin {
    @Inject(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void sphereshields$renameShieldGenerator(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        if (sphereshields$isShieldGenerator((Item) (Object) this)) {
            cir.setReturnValue(Component.literal("Хекстековое Силовое Ядро").withStyle(ChatFormatting.AQUA));
        }
    }

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void sphereshields$describeShieldGenerator(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (!sphereshields$isShieldGenerator((Item) (Object) this)) return;

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Требуется ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("редстоун-сигнал").withStyle(ChatFormatting.RED))
                .append(Component.literal(" для активации").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Топливо:").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(sphereshields$fuelLine(ChatFormatting.GOLD, "Медь", "слабый щит"));
        tooltip.add(sphereshields$fuelLine(ChatFormatting.BLUE, "Лазурит", "средний щит, растёт с сигналом"));
        tooltip.add(sphereshields$fuelLine(ChatFormatting.WHITE, "Кварц", "мощный щит, падает с сигналом"));
        tooltip.add(sphereshields$fuelLine(ChatFormatting.LIGHT_PURPLE, "Аметист", "максимальная мощность"));
        tooltip.add(Component.literal("Либо: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Forge Energy").withStyle(ChatFormatting.AQUA)));
    }

    @Unique
    private static Component sphereshields$fuelLine(ChatFormatting color, String name, String description) {
        return Component.literal(" ● ").withStyle(color)
                .append(Component.literal(name).withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal(" — " + description).withStyle(ChatFormatting.GRAY));
    }

    @Unique
    private static boolean sphereshields$isShieldGenerator(Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShieldGeneratorBlock;
    }
}
