package dev.qur1s.spellclasses.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The client side of the extra cyberpsychosis symptoms: the server rolls them on every attack (see CyberpsychosisSymptoms) and tells which to
 * show: a random key (movement, jump, sneak, drop) pressed for a second, a mouse button or the arm hardware key (whatever they are rebound to),
 * and dizziness (the camera turns by itself for five seconds). Nothing is pressed while the player lies in a surgery chair (sneak would cancel
 * the operation) or has a screen open.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CyberpsychosisKeys {
    private static final int HOLD_TICKS = 20;
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final class Hold {
        final KeyMapping key;
        int left = HOLD_TICKS;

        Hold(KeyMapping key) {
            this.key = key;
        }
    }

    private static final List<Hold> holds = new ArrayList<>();
    /** Dizziness: for five seconds the camera drifts on its own (left, up, ...), the direction changes every few ticks. */
    private static int dizzyLeft;
    private static int dizzyStep;
    private static float dizzyYaw;
    private static float dizzyPitch;

    private static KeyMapping arms;
    private static boolean armsLookedUp;

    private CyberpsychosisKeys() {
    }

    /** Sneaking cancels a Chromeware operation, so no symptom may press anything while lying in the surgery chair. */
    private static boolean onSurgerySeat(Minecraft mc) {
        var vehicle = mc.player.getVehicle();
        return vehicle != null && "com.chromeware.surgery.SurgerySeat".equals(vehicle.getClass().getName());
    }

    private static KeyMapping armsKey(Minecraft mc) {
        if (!armsLookedUp) {
            armsLookedUp = true;
            for (KeyMapping k : mc.options.keyMappings) {
                if ("key.chromeware.monowire".equals(k.getName())) {
                    arms = k;
                    break;
                }
            }
        }
        return arms;
    }

    private static void press(KeyMapping key) {
        for (Hold h : holds) {
            if (h.key == key) return;
        }
        KeyMapping.click(key.getKey());
        key.setDown(true);
        holds.add(new Hold(key));
    }

    private static boolean held(KeyMapping key) {
        for (Hold h : holds) {
            if (h.key == key) return true;
        }
        return false;
    }

    /** Movement is forced on the player's input itself, so nothing that resets the key state can undo it. */
    @SubscribeEvent
    static void onMovementInput(MovementInputUpdateEvent event) {
        if (holds.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        var input = event.getInput();
        boolean moved = false;
        if (held(mc.options.keyUp)) {
            input.up = true;
            moved = true;
        }
        if (held(mc.options.keyDown)) {
            input.down = true;
            moved = true;
        }
        if (held(mc.options.keyLeft)) {
            input.left = true;
            moved = true;
        }
        if (held(mc.options.keyRight)) {
            input.right = true;
            moved = true;
        }
        if (held(mc.options.keyJump)) input.jumping = true;
        if (held(mc.options.keyShift)) input.shiftKeyDown = true;
        if (moved) {
            float slow = input.shiftKeyDown ? 0.3F : 1.0F;
            input.forwardImpulse = ((input.up ? 1.0F : 0.0F) - (input.down ? 1.0F : 0.0F)) * slow;
            input.leftImpulse = ((input.left ? 1.0F : 0.0F) - (input.right ? 1.0F : 0.0F)) * slow;
        }
    }

    private static void releaseAll() {
        for (Hold h : holds) h.key.setDown(false);
        holds.clear();
    }

    /** Called for every cyberpsychosis attack the server rolled a symptom for. */
    public static void trigger(int mask) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive() || mc.player.isSpectator() || mc.screen != null || onSurgerySeat(mc)) return;
        RandomSource random = mc.player.getRandom();
        if ((mask & 1) != 0) {
            press(List.of(mc.options.keyUp, mc.options.keyLeft, mc.options.keyDown, mc.options.keyRight, mc.options.keyDrop,
                    mc.options.keyJump, mc.options.keyShift).get(random.nextInt(7)));
        }
        if ((mask & 2) != 0) {
            KeyMapping arm = armsKey(mc);
            int pick = random.nextInt(arm != null ? 3 : 2);
            press(pick == 0 ? mc.options.keyAttack : pick == 1 ? mc.options.keyUse : arm);
        }
        if ((mask & 4) != 0 && dizzyLeft <= 0) {
            dizzyLeft = 100;
            dizzyStep = 0;
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive() || mc.player.isSpectator() || mc.screen != null || onSurgerySeat(mc)) {
            releaseAll();
            dizzyLeft = 0;
            return;
        }
        for (int i = holds.size() - 1; i >= 0; i--) {
            Hold h = holds.get(i);
            if (--h.left <= 0) {
                h.key.setDown(false);
                holds.remove(i);
            } else {
                h.key.setDown(true);
            }
        }
        if (dizzyLeft > 0) {
            RandomSource random = mc.player.getRandom();
            dizzyLeft--;
            if (--dizzyStep <= 0) {
                dizzyStep = 6 + random.nextInt(6);
                float angle = random.nextFloat() * (float) (Math.PI * 2.0);
                dizzyYaw = Mth.cos(angle) * 22.0F;
                dizzyPitch = Mth.sin(angle) * 9.0F;
            }
            mc.player.turn(dizzyYaw, dizzyPitch);
        }
    }
}
