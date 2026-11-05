package dev.qur1s.spellclasses.client;

import net.minecraft.client.model.geom.ModelPart;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Asks Chromeware (by reflection) whether it hides a model part of the player being rendered right now. */
public final class ChromewareHidden {
    private static MethodHandle ruleFor;
    private static MethodHandle hidden;
    private static boolean looked;
    private static boolean broken;

    private ChromewareHidden() {
    }

    public static boolean isHidden(ModelPart part) {
        if (broken) return false;
        try {
            if (!looked) {
                looked = true;
                Class<?> context = Class.forName("com.chromeware.client.body.LimbRenderContext");
                Class<?> rule = Class.forName("com.chromeware.client.body.LimbRenderContext$Rule");
                var lookup = MethodHandles.publicLookup();
                ruleFor = lookup.findStatic(context, "ruleFor", MethodType.methodType(rule, ModelPart.class));
                hidden = lookup.findVirtual(rule, "hidden", MethodType.methodType(boolean.class));
            }
            Object rule = (Object) ruleFor.invoke(part);
            return rule != null && (boolean) hidden.invoke(rule);
        } catch (Throwable t) {
            broken = true;
            return false;
        }
    }
}
