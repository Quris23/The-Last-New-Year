package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.network.SeatDismountPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;

/**
 * On a Sable ship the normal sneak-to-dismount does not reach the server. A single fresh press of Shift while on a seat
 * makes the client ask the server to dismount the player - except while an Aeroworks console is being controlled
 * (Shift is a control input there) and until Shift has been released after that.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class SeatSneakClient {
    private static boolean prevDown;
    private static boolean suppress;
    private static Method consoleActive;
    private static boolean consoleLookedUp;

    private SeatSneakClient() {
    }

    private static boolean aeroworksConsoleActive() {
        if (!consoleLookedUp) {
            consoleLookedUp = true;
            try {
                consoleActive = Class.forName("com.mred231.aeroworks.content.controls.console.ConsoleControlClient")
                        .getMethod("isActive");
            } catch (Throwable ignored) {
                consoleActive = null;
            }
        }
        if (consoleActive == null) return false;
        try {
            return (boolean) consoleActive.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            prevDown = false;
            suppress = false;
            return;
        }
        boolean down = mc.options.keyShift.isDown();
        boolean edge = down && !prevDown;
        prevDown = down;

        if (aeroworksConsoleActive()) suppress = true;
        else if (!down) suppress = false;

        if (!edge || suppress || mc.screen != null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle != null && BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).getPath().contains("seat")) {
            PacketDistributor.sendToServer(new SeatDismountPayload());
        }
    }
}
