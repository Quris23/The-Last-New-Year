package dev.qur1s.atlas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A compass in one hand and a "searching eye" in the other: right-click uses the eye up and points the compass where
 * the eye would have flown. Works for the Eye of Ender and for the eyes of other mods that find a structure the same
 * way (Legendary Monsters, Cataclysm and others). Without a compass in the other hand nothing here changes how an eye behaves.
 */
@EventBusSubscriber(modid = "atlas")
public final class EyeSearch {
    /** Set while the eye's own code runs, so the eye entity it would throw is caught instead. */
    public static boolean capturing;
    public static BlockPos captured;

    private static final Map<Class<?>, Boolean> KNOWN = new HashMap<>();

    private EyeSearch() {
    }

    /** Whether the item is a searching eye: its code asks the level for the nearest structure (the Eye of Ender included). */
    public static boolean isSearchEye(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ENDER_EYE)) return true;
        Class<?> type = stack.getItem().getClass();
        return KNOWN.computeIfAbsent(type, EyeSearch::refersToEyeEntity);
    }

    private static boolean refersToEyeEntity(Class<?> type) {
        for (Class<?> c = type; c != null && c != Item.class && c != Object.class; c = c.getSuperclass()) {
            String resource = c.getName().replace('.', '/') + ".class";
            try (InputStream in = c.getClassLoader().getResourceAsStream(resource)) {
                if (in != null && new String(in.readAllBytes(), StandardCharsets.ISO_8859_1).contains("findNearestMapStructure")) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static boolean applies(Player player, InteractionHand hand) {
        return isSearchEye(player.getItemInHand(hand)) && player.getItemInHand(AtlasCompass.other(hand)).is(Items.COMPASS);
    }

    private static void run(ServerPlayer player, InteractionHand hand) {
        ItemStack eye = player.getItemInHand(hand);
        ItemStack compass = player.getItemInHand(AtlasCompass.other(hand));
        int before = eye.getCount();
        capturing = true;
        captured = null;
        try {
            eye.use(player.level(), player, hand);
        } finally {
            capturing = false;
        }
        player.stopUsingItem();
        BlockPos found = captured;
        captured = null;
        if (found == null) {
            player.displayClientMessage(Component.translatable("message.atlas.eye_nothing"), true);
            return;
        }
        // the eye is used up even if its own code did not do it, since the entity that normally breaks it was never thrown
        ItemStack now = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild && now.getCount() == before && !now.isEmpty()) now.shrink(1);
        AtlasCompass.magnetize(player, compass, GlobalPos.of(player.level().dimension(), found));
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!applies(player, event.getHand())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!event.getLevel().isClientSide && player instanceof ServerPlayer sp) run(sp, event.getHand());
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        // an eye on an end portal frame is placed in it as usual
        if (event.getLevel().getBlockState(event.getPos()).is(net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME)) return;
        if (!applies(player, event.getHand())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!event.getLevel().isClientSide && player instanceof ServerPlayer sp) run(sp, event.getHand());
    }

    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (capturing && !(event.getEntity() instanceof Player)) event.setCanceled(true);   // the eye it would throw
    }
}
