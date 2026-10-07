package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.qur1s.spellclasses.network.ScabbardSwapPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** The key that swaps the main-hand item with the scabbard slot (like the off-hand swap key). */
public final class ScabbardKey {
    public static final KeyMapping SWAP = new KeyMapping("key.spellclasses.scabbard_swap",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.spellclasses");

    private ScabbardKey() {
    }

    @EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Register {
        private Register() {
        }

        @SubscribeEvent
        static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(SWAP);
        }
    }

    @EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class Tick {
        private Tick() {
        }

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            while (SWAP.consumeClick()) {
                if (mc.player != null && mc.screen == null) {
                    PacketDistributor.sendToServer(new ScabbardSwapPayload());
                }
            }
        }
    }
}
