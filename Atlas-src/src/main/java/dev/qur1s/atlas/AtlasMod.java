package dev.qur1s.atlas;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(AtlasMod.MODID)
public class AtlasMod {
    public static final String MODID = "atlas";

    public AtlasMod(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        AtlasComponents.register(modBus);
        AtlasItems.register(modBus);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, AtlasConfig.SPEC);
    }
}
