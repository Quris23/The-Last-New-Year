package dev.qur1s.spellclasses.implant;

import com.mojang.logging.LogUtils;

import java.lang.reflect.Field;

/**
 * Chromeware fixes the immunosuppressant durations inside its Grade enum (Standard 10 min, Military 3 min) with no config option,
 * so the final {@code protection} fields are rewritten at startup: Standard 1 hour, Military 40 minutes.
 */
public final class ImmunosuppressantTimes {
    private static final int STANDARD_TICKS = 60 * 60 * 20;
    private static final int MILITARY_TICKS = 40 * 60 * 20;

    private ImmunosuppressantTimes() {
    }

    public static void apply() {
        try {
            Class<?> grade = Class.forName("com.chromeware.humanity.Suppression$Grade");
            Field protection = grade.getField("protection");
            protection.setAccessible(true);
            for (Object constant : grade.getEnumConstants()) {
                String name = ((Enum<?>) constant).name();
                if (name.equals("STANDARD")) protection.setInt(constant, STANDARD_TICKS);
                else if (name.equals("MILITARY")) protection.setInt(constant, MILITARY_TICKS);
            }
        } catch (Throwable t) {
            LogUtils.getLogger().warn("Cannot change the immunosuppressant durations", t);
        }
    }
}
