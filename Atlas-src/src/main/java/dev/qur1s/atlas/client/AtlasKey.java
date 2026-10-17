package dev.qur1s.atlas.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.qur1s.atlas.Atlases;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** The key that opens the Atlas from its Curios slot (the key that is "Ь" on a Russian layout, i.e. M, by default). */
public final class AtlasKey {
    public static final KeyMapping OPEN = new KeyMapping("key.atlas.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.atlas");

    private AtlasKey() {
    }

    @EventBusSubscriber(modid = "atlas", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Register {
        private Register() {
        }

        @SubscribeEvent
        static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
        }
    }

    @EventBusSubscriber(modid = "atlas", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class Tick {
        private Tick() {
        }

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            while (OPEN.consumeClick()) {
                if (mc.player == null || mc.screen != null) continue;
                if (Atlases.hasEquipped(mc.player)) {
                    java.util.UUID id = Atlases.equippedId(mc.player);
                    if (id != null) {
                        AtlasClient.open(id);
                    } else {
                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new dev.qur1s.atlas.network.RequestOpenPayload());
                    }
                } else {
                    mc.player.displayClientMessage(Component.translatable("message.atlas.not_equipped"), true);
                }
            }
        }
    }
}
