package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.InscribeSpellEvent;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;

/**
 * Заклинания школ Бездны и Песка (cataclysm_spellbooks) можно вписать в Инкрустационном столе только
 * в их собственные гримуары - никуда больше. Всё остальное (другие школы, другие книги) не трогаем.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class GrimoireSchoolLockEvents {
    private GrimoireSchoolLockEvents() {
    }

    private static final Map<ResourceLocation, ResourceLocation> SCHOOL_LOCKED_TO_BOOK = Map.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyss_spell_book"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "sand"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "desert_spell_book")
    );

    @SubscribeEvent
    static void onInscribe(InscribeSpellEvent event) {
        ResourceLocation schoolId = event.getSpellData().getSpell().getSchoolType().getId();
        ResourceLocation requiredBook = SCHOOL_LOCKED_TO_BOOK.get(schoolId);
        if (requiredBook == null) return;

        Player player = event.getEntity();
        if (!(player.containerMenu instanceof InscriptionTableMenu table)) return;

        ItemStack bookStack = table.getSpellBookSlot().getItem();
        ResourceLocation bookId = BuiltInRegistries.ITEM.getKey(bookStack.getItem());
        if (requiredBook.equals(bookId)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.translatable(
                "spellclasses.message.wrong_grimoire",
                Component.translatable("item." + requiredBook.getNamespace() + "." + requiredBook.getPath())
        ).withStyle(ChatFormatting.RED));
    }
}
