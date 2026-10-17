package dev.qur1s.legspyglass.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.qur1s.legspyglass.LegSpyglass;
import dev.qur1s.legspyglass.Spyglasses;
import dev.qur1s.legspyglass.network.ScopeBroadcastPayload;
import dev.qur1s.legspyglass.network.ScopeStatePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/** Client side: the Z key, the zoom, the scope overlay, and which players are looking through the spyglass. */
public final class ClientScope {
    private ClientScope() {
    }

    public static final KeyMapping KEY = new KeyMapping("key.legspyglass.use", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z, "key.categories.legspyglass");

    private static final float ZOOM_MIN = 1.5f;
    private static final float ZOOM_MAX = 40.0f;
    private static final float ZOOM_START = 4.0f;

    private static final Set<Integer> SCOPING_IDS = new HashSet<>();
    private static boolean scoping;
    private static float zoom = ZOOM_START;
    private static float shownZoom = 1f;
    private static boolean savedSmoothCamera;

    /** True while the local player holds the key and has the spyglass in the slot. */
    public static boolean isLocalScoping() {
        return scoping;
    }

    public static boolean isScoping(Player player) {
        return SCOPING_IDS.contains(player.getId());
    }

    public static void onBroadcast(ScopeBroadcastPayload payload) {
        if (payload.scoping()) SCOPING_IDS.add(payload.entityId());
        else SCOPING_IDS.remove(payload.entityId());
    }

    private static void setScoping(Minecraft mc, boolean value) {
        if (scoping == value) return;
        scoping = value;
        if (value) {
            savedSmoothCamera = mc.options.smoothCamera;
            mc.options.smoothCamera = true; // slows the mouse down like the vanilla spyglass does
            zoom = Math.max(zoom, ZOOM_MIN);
            shownZoom = 1f;
        } else {
            mc.options.smoothCamera = savedSmoothCamera;
        }
        PacketDistributor.sendToServer(new ScopeStatePayload(value));
    }

    @EventBusSubscriber(modid = LegSpyglass.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY);
        }
    }

    @EventBusSubscriber(modid = LegSpyglass.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {
        }

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                if (scoping) scoping = false;
                return;
            }
            boolean want = KEY.isDown() && mc.screen == null && Spyglasses.hasSlotted(mc.player);
            setScoping(mc, want);
            // Ease the shown zoom towards the target so wheel steps do not jump.
            float target = scoping ? zoom : 1f;
            shownZoom += (target - shownZoom) * 0.75f;
        }

        @SubscribeEvent
        static void onScroll(InputEvent.MouseScrollingEvent event) {
            if (!scoping) return;
            double delta = event.getScrollDeltaY();
            if (delta != 0) {
                zoom = Mth.clamp(zoom * (float) Math.pow(1.18, delta), ZOOM_MIN, ZOOM_MAX);
            }
            event.setCanceled(true); // the wheel must not switch the hotbar slot while looking
        }

        @SubscribeEvent
        static void onFov(ComputeFovModifierEvent event) {
            if (scoping || shownZoom > 1.01f) {
                event.setNewFovModifier(event.getNewFovModifier() / Math.max(1f, shownZoom));
            }
        }

        @SubscribeEvent
        static void onRenderHand(RenderHandEvent event) {
            if (scoping) event.setCanceled(true);
        }

        @SubscribeEvent
        static void onCrosshair(RenderGuiLayerEvent.Pre event) {
            if (scoping && event.getName().equals(VanillaGuiLayers.CROSSHAIR)) event.setCanceled(true);
        }

        @SubscribeEvent
        static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            SCOPING_IDS.clear();
            Minecraft mc = Minecraft.getInstance();
            if (scoping) mc.options.smoothCamera = savedSmoothCamera;
            scoping = false;
        }
    }
}
