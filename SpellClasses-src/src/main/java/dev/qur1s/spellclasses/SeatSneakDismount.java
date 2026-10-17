package dev.qur1s.spellclasses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Gets a player off a stuck seat entity (Create seats on Sable ships); triggered by a dedicated key. */
public final class SeatSneakDismount {
    private SeatSneakDismount() {
    }

    public static void dismount(ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        if (!id.getPath().contains("seat")) return;
        player.stopRiding();
        if (vehicle.isAlive() && vehicle.getPassengers().isEmpty()) vehicle.discard();
    }
}
