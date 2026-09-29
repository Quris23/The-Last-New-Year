package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Set;

/**
 * By user design: every Nirvana-mod craft (crafting table + furnace-family recipes) requires the
 * Nature class (Druid) to have been chosen - except the Joint, which stays open to everyone.
 * Breaking the wild hemp bush is likewise Druid-only, so non-Druids can't even gather the mod's core
 * ingredient in the first place.
 * <p>
 * {@link PlayerEvent.ItemCraftedEvent} / {@link PlayerEvent.ItemSmeltedEvent} aren't cancellable -
 * they fire from inside the result slot's {@code onTake}, before the stack is handed off to the
 * player's inventory, so zeroing the (mutable) crafted stack in place voids the craft just as
 * effectively as cancelling would.
 * <p>
 * Nirvana's own "suspicious_crafting" type (Herbal Salve, Suspicious Pipe) is a {@code CustomRecipe}
 * used inside the ordinary crafting table grid (just with randomized Suspicious-Stew-style effects),
 * so it still goes through the vanilla result slot and is covered here like any other recipe.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class NirvanaClassGate {
    private NirvanaClassGate() {
    }

    private static final String NIRVANA_NAMESPACE = "nirvana";
    private static final Set<ResourceLocation> EXEMPT_RESULTS = Set.of(
            ResourceLocation.fromNamespaceAndPath(NIRVANA_NAMESPACE, "joint")
    );
    private static final ResourceLocation WILD_HEMP = ResourceLocation.fromNamespaceAndPath(NIRVANA_NAMESPACE, "wild_hemp");

    @SubscribeEvent
    static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        gate(event.getEntity() instanceof ServerPlayer sp ? sp : null, event.getCrafting());
    }

    @SubscribeEvent
    static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        gate(event.getEntity() instanceof ServerPlayer sp ? sp : null, event.getSmelting());
    }

    private static void gate(ServerPlayer player, ItemStack result) {
        if (player == null || result.isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (!NIRVANA_NAMESPACE.equals(id.getNamespace()) || EXEMPT_RESULTS.contains(id)) return;
        if (isDruid(player)) return;

        result.setCount(0);
        player.sendSystemMessage(Component.translatable("spellclasses.message.requires_druid_nirvana").withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    static void onBlockBreak(BlockEvent.BreakEvent event) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        if (!WILD_HEMP.equals(blockId)) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (isDruid(player)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.translatable("spellclasses.message.requires_druid_nirvana").withStyle(ChatFormatting.RED));
    }

    private static boolean isDruid(ServerPlayer player) {
        return SchoolRegistry.NATURE_RESOURCE.equals(ClassManager.chosenSchool(player).orElse(null));
    }
}
