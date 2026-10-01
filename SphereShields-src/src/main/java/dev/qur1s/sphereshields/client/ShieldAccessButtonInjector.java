package dev.qur1s.sphereshields.client;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import com.anton.shieldgenerators.ShieldGeneratorScreen;
import dev.qur1s.sphereshields.AllowedPlayersHolder;
import dev.qur1s.sphereshields.MobAccessHolder;
import dev.qur1s.sphereshields.mixin.ContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.LinkedHashSet;

/**
 * Adds the shield GUI's access-control button on top of {@code ShieldGeneratorScreen} without
 * needing to mixin-override its {@code init()} - {@code ScreenEvent.Init.Post} is the standard hook
 * for bolting a widget onto a screen owned by another mod.
 */
@EventBusSubscriber(modid = "sphereshields", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ShieldAccessButtonInjector {
    private ShieldAccessButtonInjector() {
    }

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ShieldGeneratorScreen screen)) return;

        BlockPos pos = LastOpenedGeneratorTracker.get();
        if (pos == null || Minecraft.getInstance().level == null) return;

        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (!(be instanceof ShieldGeneratorBlockEntity blockEntity) || !(blockEntity instanceof AllowedPlayersHolder holder)) return;
        if (!(blockEntity instanceof MobAccessHolder mobAccess)) return;

        // Placed to the LEFT of the panel rather than to the right: the right side is where JEI
        // docks its ingredient list, and a widget placed there ends up rendered underneath it.
        ContainerScreenAccessor screenAccessor = (ContainerScreenAccessor) screen;
        int x = screenAccessor.sphereshields$getLeftPos() - 74;
        int y = screenAccessor.sphereshields$getTopPos();

        ThemedButton button = new ThemedButton(x, y, 70, 20, Component.literal("Доступ"), b -> {
            var currentlyAllowed = new LinkedHashSet<>(holder.sphereshields$getAllowedPlayers());
            Minecraft.getInstance().setScreen(new PlayerAccessScreen(screen, pos, currentlyAllowed, mobAccess.sphereshields$isAllowMobs()));
        });
        button.setHighlighted(true);

        event.addListener(button);
    }
}
