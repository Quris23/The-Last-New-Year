package dev.qur1s.spellclasses.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private ModEntities() {
    }

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "spellclasses");

    public static final DeferredHolder<EntityType<?>, EntityType<LightShieldEntity>> LIGHT_SHIELD = ENTITIES.register(
            "light_shield",
            () -> EntityType.Builder.<LightShieldEntity>of(LightShieldEntity::new, MobCategory.MISC)
                    .sized(LightShieldEntity.RADIUS * 2, LightShieldEntity.RADIUS * 2)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .fireImmune()
                    .noSave()
                    .build("light_shield"));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
