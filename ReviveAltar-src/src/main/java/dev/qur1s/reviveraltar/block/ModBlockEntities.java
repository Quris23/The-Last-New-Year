package dev.qur1s.reviveraltar.block;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.BuiltInRegistries;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), ReviveAltarMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR =
            REGISTER.register("ritual_altar", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(RitualAltarBlockEntity::new, ModBlocks.RITUAL_ALTAR.get())
                    .build(null));
}
