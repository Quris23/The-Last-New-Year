package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * The Portal spell can only be cast while holding the Hither-Thither Wand or the Void Staff (see
 * {@link VoidStaffPortalSpell}) - not from any other item it might get inscribed/learned into.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class PortalCastGateEvents {
    private PortalCastGateEvents() {
    }

    private static final String PORTAL_SPELL_ID = "irons_spellbooks:portal";

    @SubscribeEvent
    static void onPreCast(SpellPreCastEvent event) {
        if (!PORTAL_SPELL_ID.equals(event.getSpellId())) return;

        Player player = event.getEntity();
        Item mainHand = player.getMainHandItem().getItem();
        if (mainHand instanceof io.redspace.ironsspellbooks.item.weapons.HitherThitherWand) return;
        if (isVoidStaff(mainHand)) return;

        event.setCanceled(true);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("spellclasses.message.requires_void_staff")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static boolean isVoidStaff(Item item) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return id.getNamespace().equals("cataclysm_spellbooks") && id.getPath().equals("void_staff");
    }
}
