package dev.qur1s.spellclasses.client;

import net.minecraft.client.Minecraft;

/**
 * State of the typed-in value of an R/G/B channel in Chromeware's implant colour page (the number next to a slider
 * can be clicked and typed over). Keys are fed in from the implant screen, drawing and clicks from the colour page.
 */
public final class ColorValueEdit {
    private ColorValueEdit() {
    }

    public static boolean active;
    public static int channel = -1;
    public static String buffer = "";
    /** What the colour page has to apply on its next frame: the channel and its new value (0..255), or -1. */
    public static int pendingChannel = -1;
    public static int pendingValue;
    /** Which implant/part the edit belongs to, taken when it started. */
    public static int implantIndex;
    public static int partIndex;

    public static void start(int channelIndex, int current, int implant, int part) {
        if (active) commit();
        active = true;
        channel = channelIndex;
        buffer = Integer.toString(current);
        implantIndex = implant;
        partIndex = part;
    }

    public static void cancel() {
        active = false;
        channel = -1;
        buffer = "";
    }

    public static void commit() {
        if (active && !buffer.isEmpty()) {
            try {
                pendingValue = channel == HEX ? Integer.parseInt(buffer, 16) : Math.min(255, Integer.parseInt(buffer));
                pendingChannel = channel;
            } catch (NumberFormatException ignored) {
            }
        }
        cancel();
    }

    /** Channel index of the hex field (#RRGGBB); 0..2 are the R/G/B numbers. */
    public static final int HEX = 3;

    private static void append(String digits) {
        boolean hex = channel == HEX;
        for (char c : digits.toCharArray()) {
            if (hex) {
                if (Character.digit(c, 16) >= 0 && buffer.length() < 6) buffer += Character.toUpperCase(c);
            } else if (c >= '0' && c <= '9' && buffer.length() < 3) {
                buffer += c;
            }
        }
    }

    /** Returns true if the key was consumed. */
    public static boolean key(int keyCode, int modifiers) {
        if (!active) return false;
        if (keyCode == 256) { // Esc
            cancel();
        } else if (keyCode == 257 || keyCode == 335 || keyCode == 258) { // Enter, numpad Enter, Tab
            commit();
        } else if (keyCode == 259) { // Backspace
            if (!buffer.isEmpty()) buffer = buffer.substring(0, buffer.length() - 1);
        } else if (keyCode >= 48 && keyCode <= 57 && (modifiers & 0xA) == 0) {
            append(String.valueOf((char) keyCode));
        } else if (channel == HEX && keyCode >= 65 && keyCode <= 70 && (modifiers & 0xA) == 0) {
            append(String.valueOf((char) keyCode));
        } else if (keyCode >= 320 && keyCode <= 329) { // numpad
            append(String.valueOf((char) ('0' + keyCode - 320)));
        } else if (keyCode == 86 && (modifiers & 0xA) != 0) { // Ctrl/Cmd+V
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            buffer = "";
            append(clip == null ? "" : clip.trim().replace("#", ""));
        }
        return true; // while typing, nothing else (E, A/D, ...) may act on the screen
    }
}
