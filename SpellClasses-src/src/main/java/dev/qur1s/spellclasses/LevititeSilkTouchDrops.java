package dev.qur1s.spellclasses;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Create Aeronautics registers {@code aeronautics:levitite}/{@code pearlescent_levitite} with
 * {@code Properties::noLootTable}, which hard-codes the block's loot table reference to vanilla's
 * shared empty table ({@code minecraft:blocks/empty}) rather than its own per-block path - a KubeJS
 * loot table edit at the usual {@code data/aeronautics/loot_table/blocks/levitite.json} path is never
 * consulted, and editing the shared empty table would affect every no-drop block in the game. The
 * mod's own ponder text says the crystallized block "cannot be recollected" by design (it's meant as
 * a structural/buoyancy byproduct, not a farmable resource).
 * <p>
 * By user design, made Silk Touch harvestable anyway: this hooks the drop-assembly event directly
 * (after the game's own empty-loot-table lookup already ran) and adds the block itself to the drop
 * list when the breaking tool has Silk Touch, bypassing the hard-coded empty table entirely without
 * touching it.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class LevititeSilkTouchDrops {
    private LevititeSilkTouchDrops() {
    }

    private static final ResourceLocation LEVITITE = ResourceLocation.fromNamespaceAndPath("aeronautics", "levitite");
    private static final ResourceLocation PEARLESCENT_LEVITITE = ResourceLocation.fromNamespaceAndPath("aeronautics", "pearlescent_levitite");

    @SubscribeEvent
    static void onBlockDrops(BlockDropsEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (!id.equals(LEVITITE) && !id.equals(PEARLESCENT_LEVITITE)) return;

        ItemStack tool = event.getTool();
        if (!(event.getLevel() instanceof Level level)) return;

        Holder<Enchantment> silkTouch = level.registryAccess().holderOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) <= 0) return;

        event.getDrops().add(new ItemEntity(level,
                event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                new ItemStack(block)));
    }
}
