package dev.qur1s.spellclasses.network;

import dev.qur1s.spellclasses.ClassManager;
import dev.qur1s.spellclasses.ClassSchools;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class NetworkHandler {
    private NetworkHandler() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ChooseClassPayload.TYPE, ChooseClassPayload.STREAM_CODEC, NetworkHandler::handleChooseClass);
    }

    private static void removeOneClassBook(ServerPlayer player) {
        var book = dev.qur1s.spellclasses.item.ClassItems.CLASS_BOOK.get();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(book)) {
                stack.shrink(1);
                break;
            }
        }
    }

    private static void handleChooseClass(ChooseClassPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ResourceLocation school = ResourceLocation.tryParse(payload.schoolId());
            if (school == null || !ClassSchools.restrictedSchools().contains(school)) return;

            if (ClassManager.hasChosen(player)) {
                player.sendSystemMessage(Component.translatable("spellclasses.message.already_chosen").withStyle(ChatFormatting.RED));
                return;
            }
            if (!ClassManager.tryChooseSchool(player, school)) {
                player.sendSystemMessage(Component.translatable("spellclasses.message.class_full").withStyle(ChatFormatting.RED));
                return;
            }
            removeOneClassBook(player);
            player.sendSystemMessage(Component.translatable("spellclasses.message.class_chosen",
                    ClassSchools.className(school)).withStyle(ChatFormatting.GREEN));
        });
    }
}
