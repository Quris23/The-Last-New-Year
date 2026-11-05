package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.network.TidePiercerPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Tells the server that the use button is held with the Tide Piercer in the hand (the server does the rest, see TidePiercerDrink). */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class TidePiercerKey {
    private TidePiercerKey() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.player.tickCount % 2 != 0 || !mc.options.keyUse.isDown()) return;
        var id = BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem());
        if (id.getNamespace().equals("somakespells") && id.getPath().equals("tide_piercer")) {
            PacketDistributor.sendToServer(new TidePiercerPayload());
        }
    }
}
