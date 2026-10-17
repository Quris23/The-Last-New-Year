package dev.qur1s.reviveraltar;

import dev.qur1s.reviveraltar.block.ModBlockEntities;
import dev.qur1s.reviveraltar.block.ModBlocks;
import dev.qur1s.reviveraltar.data.ModAttachments;
import dev.qur1s.reviveraltar.event.CommonEventHandlers;
import dev.qur1s.reviveraltar.event.ModEventHandlers;
import dev.qur1s.reviveraltar.network.ModNetworking;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ReviveAltarMod.MODID)
public class ReviveAltarMod {
    public static final String MODID = "reviveraltar";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public ReviveAltarMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.REGISTER.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.REGISTER.register(modEventBus);
        ModAttachments.REGISTER.register(modEventBus);

        modEventBus.register(new ModEventHandlers());
        modEventBus.register(new ModNetworking());
        NeoForge.EVENT_BUS.register(new CommonEventHandlers());
    }
}
