package dev.qur1s.spellclasses.client;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Runs a draw of a Chromeware arm inside Chromeware's own Hemodynamics wrapper. Declaring it as blade hardware makes every quad of
 * the cyber arm take the arm's blood directly; otherwise quads close to the vanilla arm cube only show stains painted on that
 * exact face, which is why the arm looked clean while the bulkier combat hardware was bloody.
 */
public final class ArmBlood {
    private static MethodHandle arm;
    private static boolean looked;
    private static boolean broken;

    private ArmBlood() {
    }

    /** Runs {@code draw} exactly once, wrapped when Chromeware's Hemodynamics bridge is available. */
    public static void around(LivingEntity entity, HumanoidArm side, Runnable draw) {
        if (broken) {
            draw.run();
            return;
        }
        boolean[] ran = {false};
        Runnable once = () -> {
            ran[0] = true;
            draw.run();
        };
        try {
            if (!looked) {
                looked = true;
                arm = MethodHandles.publicLookup().findStatic(Class.forName("com.chromeware.compat.HemodynamicsCompatibility"),
                        "arm", MethodType.methodType(void.class, LivingEntity.class, HumanoidArm.class, boolean.class, Runnable.class));
            }
            arm.invoke(entity, side, true, once);
        } catch (Throwable t) {
            broken = true;
            if (!ran[0]) draw.run();
        }
    }
}
