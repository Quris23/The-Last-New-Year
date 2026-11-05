package dev.qur1s.spellclasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Create: Wizardry's Mana Siphon renderer crashes the game when its partial models (wheel and prong) are not baked:
 * CachedBuffers.partial is handed a null BakedModel. Reflection keeps the check independent of the mod being on the classpath.
 */
public final class ManaSiphonGuard {
    private static final String[] PARTIALS = {"MANA_SIPHON_WHEEL", "MANA_SIPHON_PRONG"};
    private static final Object[] FIELDS = new Object[PARTIALS.length];
    private static Method get;
    private static boolean looked;
    private static boolean warned;

    private ManaSiphonGuard() {
    }

    /**
     * Create: Wizardry registers its Flywheel partial models in FMLClientSetupEvent.enqueueWork, which runs after the first model
     * bake, so on a fresh launch they are never baked until something reloads the resources (changing a resource pack, F3+T).
     * Initialising the classes during mod construction registers them in time for the first bake.
     */
    public static void registerPartialsEarly() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String name : new String[] {
                "net.ttzplayz.create_wizardry.client.CWPartialModels",
                "net.ttzplayz.create_wizardry.client.pipe.ArcanePartialModels"}) {
            try {
                Class.forName(name, true, cl);
            } catch (Throwable t) {
                System.err.println("[SpellClasses] Could not register " + name + " early: " + t);
            }
        }
    }

    /** True when every partial model the renderer needs has been baked (or the check cannot be made). */
    public static boolean ready() {
        try {
            if (!looked) {
                looked = true;
                Class<?> c = Class.forName("net.ttzplayz.create_wizardry.client.CWPartialModels");
                for (int i = 0; i < PARTIALS.length; i++) {
                    Field f = c.getField(PARTIALS[i]);
                    FIELDS[i] = f.get(null);
                }
                get = FIELDS[0].getClass().getMethod("get");
            }
            if (get == null) return true;
            for (Object pm : FIELDS) {
                if (pm != null && get.invoke(pm) == null) {
                    if (!warned) {
                        warned = true;
                        System.err.println("[SpellClasses] Mana Siphon partial model is not baked; skipping its renderer to avoid a crash.");
                    }
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            get = null;
            return true;
        }
    }
}
