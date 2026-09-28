package dev.qur1s.spellclasses;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Per-class player caps, e.g. "only one Ender class on the server". Empty by default (no cap
 * for any class). Entries look like {@code "irons_spellbooks:ender=1"}; a school with no entry,
 * or with a value {@code <= 0}, has no limit. Enforced in {@link ClassManager#canJoinSchool}.
 */
public final class ClassConfig {
    private ClassConfig() {
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CLASS_LIMITS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLASS_LIMITS = builder
                .comment(
                        "Per-class player caps. One entry per limited school, as \"schoolId=max\", e.g.:",
                        "  \"irons_spellbooks:ender=1\"",
                        "A school with no entry here, or max <= 0, has no limit.",
                        "Valid school ids: irons_spellbooks:fire, ice, lightning, holy, ender, blood, nature",
                        "(evocation and eldritch are never restricted, so a limit here would do nothing).")
                .defineListAllowEmpty("classLimits", List.of(), () -> "namespace:school=max", o -> o instanceof String);
        SPEC = builder.build();
    }
}
