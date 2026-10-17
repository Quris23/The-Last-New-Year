package dev.qur1s.reviveraltar.block;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER =
            DeferredRegister.createBlocks(ReviveAltarMod.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ReviveAltarMod.MODID);

    public static final DeferredBlock<RitualAltarBlock> RITUAL_ALTAR = REGISTER.register("ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0F, 15.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 15 : 3)));

    public static final DeferredItem<BlockItem> RITUAL_ALTAR_ITEM = ITEMS.registerSimpleBlockItem(
            "ritual_altar", RITUAL_ALTAR, new Item.Properties());
}
