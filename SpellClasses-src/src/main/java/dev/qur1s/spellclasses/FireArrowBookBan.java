package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Fire Arrow («Отворись») cannot be cast from a spellbook: only from a scroll or from an item it is engraved on. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class FireArrowBookBan {
    private FireArrowBookBan() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onPreCast(SpellPreCastEvent event) {
        if (event.getCastSource() != CastSource.SPELLBOOK || !"irons_spellbooks:fire_arrow".equals(event.getSpellId())) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("spellclasses.message.fire_arrow_no_book").withStyle(ChatFormatting.RED));
        }
    }
}
