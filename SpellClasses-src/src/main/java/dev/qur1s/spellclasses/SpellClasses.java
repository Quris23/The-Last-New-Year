package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.data.ClassAttachments;
import dev.qur1s.spellclasses.item.ClassEffects;
import dev.qur1s.spellclasses.item.ClassItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(SpellClasses.MODID)
public class SpellClasses {
    public static final String MODID = "spellclasses";

    public SpellClasses(IEventBus modBus, ModContainer container) {
        ClassAttachments.register(modBus);
        ClassEffects.register(modBus);
        ClassItems.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, ClassConfig.SPEC);
        modBus.addListener((ModConfigEvent event) -> {
            if (event.getConfig().getSpec() == ClassConfig.SPEC) ClassManager.invalidateLimitsCache();
        });
        modBus.addListener(ChargedBuffTweaks::onCommonSetup);
    }
}
