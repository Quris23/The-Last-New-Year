package dev.qur1s.spellclasses.compat;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import io.redspace.ironsspellbooks.entity.spells.cone_of_cold.ConeOfColdProjectile;
import io.redspace.ironsspellbooks.entity.spells.dragon_breath.DragonBreathProjectile;
import io.redspace.ironsspellbooks.entity.spells.fire_breath.FireBreathProjectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Iron's Spellbooks breath cones act like a fan blowing through the matching catalyst: Dragon Breath "ends" what it is aimed
 * at (Create: Dragons Plus "Bulk Ending"), Fire Breath smokes it (Create "Bulk Smoking", and it lights campfires) and Cone of Cold
 * freezes it (Create: Dragons Plus "Bulk Freezing").
 * Items lying on the ground and items on a depot (or any belt-like surface) inside the cone are processed. The real work
 * is in {@link BreathProcessing}, which is only touched when Create is installed.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class DragonBreathEnding {
    private DragonBreathEnding() {
    }

    @SubscribeEvent
    static void onConeTick(EntityTickEvent.Post event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        boolean dragon = event.getEntity() instanceof DragonBreathProjectile;
        boolean fire = event.getEntity() instanceof FireBreathProjectile;
        boolean frost = event.getEntity() instanceof ConeOfColdProjectile;
        if (!dragon && !fire && !frost) return;
        ModList mods = ModList.get();
        if (!mods.isLoaded("create")) return;
        if (dragon && mods.isLoaded("create_dragons_plus")) {
            DragonBreathEndingHooks.process(level, (DragonBreathProjectile) event.getEntity());
        } else if (frost && mods.isLoaded("create_dragons_plus")) {
            DragonBreathEndingHooks.processFrost(level, (ConeOfColdProjectile) event.getEntity());
        } else if (fire) {
            BreathProcessing.process(level, (FireBreathProjectile) event.getEntity(), AllFanProcessingTypes.SMOKING, true);
        }
    }
}
