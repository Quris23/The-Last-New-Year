package dev.qur1s.fluidunifier;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.List;

@Mod(FluidUnifier.MODID)
public class FluidUnifier {
    public static final String MODID = "fluidunifier";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ALIASES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ALIASES = builder
                .comment("Fluids to merge, as \"alias=canonical\". Every FluidStack of the alias becomes the canonical fluid;",
                        "the alias's flowing variant is mapped too. Entries whose alias mod isn't installed are ignored.")
                .defineListAllowEmpty("aliases", List.of(
                        "create_submarine:oxygen=fluidunifier:oxygen",
                        "northstar:oxygen=fluidunifier:oxygen"
                ), () -> "namespace:fluid=namespace:fluid", o -> o instanceof String);
        SPEC = builder.build();
    }

    public FluidUnifier(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        ModFluids.register(modEventBus);
        modEventBus.addListener(FMLCommonSetupEvent.class, e -> Canon.rebuild(ALIASES.get()));
    }
}
