package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.implant.KSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Text in the top left corner for the items in the К sockets. Altimeter: "Y: ...". Compass: "X: ... Z: ...", the direction the
 * player looks in and where a magnetized compass points (a lodestone target).
 */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class KHud {
    private static final String[] NAMES = {"Север", "Северо-восток", "Восток", "Юго-восток", "Юг", "Юго-запад", "Запад", "Северо-запад"};
    private static final int X = 6;
    private static final int Y = 6;
    private static final int LIME = 0xFF80FF20;

    private KHud() {
    }

    /** Index of the 8 compass directions of a yaw (0 = south in Minecraft, so north is 180). */
    private static int sector(double yawFromSouth) {
        double degrees = (yawFromSouth + 180.0) % 360.0; // 0 = north, clockwise
        if (degrees < 0) degrees += 360.0;
        return (int) Math.floor((degrees + 22.5) / 45.0) % 8;
    }

    /** Where a target is: an arrow relative to where the player looks. */
    private static String pointer(Player player, double tx, double tz) {
        double dx = tx - player.getX();
        double dz = tz - player.getZ();
        double yawToTarget = Math.toDegrees(Math.atan2(-dx, dz)); // Minecraft yaw of the direction to the target
        double relative = ((yawToTarget - player.getYRot()) % 360.0 + 540.0) % 360.0 - 180.0; // -180..180, 0 = straight ahead
        String arrow = Math.abs(relative) <= 45 ? "↑" : Math.abs(relative) >= 135 ? "↓" : relative > 0 ? "→" : "←";
        return arrow;
    }

    @SubscribeEvent
    static void onRender(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || mc.level == null) return;
        ItemStack altimeter = KSlots.find(player, KSlots::isAltimeter);
        ItemStack compass = KSlots.find(player, KSlots::isCompass);
        if (altimeter.isEmpty() && compass.isEmpty()) return;

        List<String> lines = new ArrayList<>();
        // one line in the order X Y Z: Y only with an altimeter, X and Z only with a compass
        StringBuilder position = new StringBuilder();
        if (!compass.isEmpty()) position.append("X: ").append((int) Math.floor(player.getX()));
        if (!altimeter.isEmpty()) position.append(position.length() > 0 ? "  " : "").append("Y: ").append((int) Math.floor(player.getY()));
        if (!compass.isEmpty()) position.append("  Z: ").append((int) Math.floor(player.getZ()));
        lines.add(position.toString());
        if (!compass.isEmpty()) {
            var tracker = compass.get(DataComponents.LODESTONE_TRACKER);
            if (tracker != null && tracker.target().isPresent()) {
                GlobalPos target = tracker.target().get();
                if (target.dimension().equals(mc.level.dimension())) {
                    lines.add(pointer(player, target.pos().getX() + 0.5, target.pos().getZ() + 0.5));
                }
            }
        }
        var g = event.getGuiGraphics();
        int y = Y;
        for (String line : lines) {
            g.drawString(mc.font, line, X, y, LIME, true);
            y += 10;
        }
    }
}
