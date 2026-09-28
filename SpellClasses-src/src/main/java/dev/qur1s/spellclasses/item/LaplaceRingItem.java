package dev.qur1s.spellclasses.item;

import dev.qur1s.spellclasses.data.ClassAttachments;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class LaplaceRingItem extends CurioBaseItem {
    public static final int EFFECT_DURATION_TICKS = 5 * 60 * 20;
    public static final int COOLDOWN_TICKS = 10 * 60 * 20;

    public LaplaceRingItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof ServerPlayer player)) return;

        long now = player.level().getGameTime();
        long readyAt = player.getData(ClassAttachments.LAPLACE_COOLDOWN_UNTIL);
        if (now < readyAt) {
            long remainingSeconds = (readyAt - now) / 20;
            player.displayClientMessage(
                    Component.translatable("spellclasses.message.laplace_on_cooldown", remainingSeconds), true);
            return;
        }

        player.setData(ClassAttachments.LAPLACE_COOLDOWN_UNTIL, now + COOLDOWN_TICKS);
        player.addEffect(new MobEffectInstance(ClassEffects.LAPLACE_FACTOR, EFFECT_DURATION_TICKS, 0, false, true, true));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof ServerPlayer player)) return;

        player.removeEffect(ClassEffects.LAPLACE_FACTOR);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.spellclasses.laplace_ring.lore.legend")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.spellclasses.laplace_ring.lore.effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.spellclasses.laplace_ring.lore.aftermath")
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.spellclasses.laplace_ring.lore.cooldown")
                .withStyle(ChatFormatting.GRAY));
    }
}
