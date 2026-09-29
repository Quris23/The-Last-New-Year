package dev.qur1s.sphereshields.client;

/**
 * Colors lifted directly from {@code ShieldGeneratorScreen}'s own private constants (via bytecode
 * inspection - PANEL_COLOR/PANEL_DARK/PANEL_EDGE/TEXT_COLOR/MUTED_TEXT_COLOR), so our own access
 * screen and its trigger button match the base mod's look instead of using stock grey widgets.
 */
final class SphereShieldsGuiStyle {
    private SphereShieldsGuiStyle() {
    }

    static final int PANEL_COLOR = 0xFF1E2630;
    static final int PANEL_DARK = 0xFF0C1117;
    static final int PANEL_EDGE = 0xFF6D879C;
    static final int TEXT_COLOR = 0xFFE8F5FF;
    static final int MUTED_TEXT_COLOR = 0xFF9FB3C3;
    /** Not lifted from the base mod - a brighter accent for hovered/checked state. */
    static final int ACCENT = 0xFF3FC7FF;
}
