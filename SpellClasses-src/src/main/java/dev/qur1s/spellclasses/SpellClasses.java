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
        dev.qur1s.spellclasses.entity.ModEntities.register(modBus);
        dev.qur1s.spellclasses.spell.ModSpells.register(modBus);
        dev.qur1s.spellclasses.item.ArmorItems.register(modBus);
        if (net.neoforged.fml.ModList.get().isLoaded("sophisticatedbackpacks") && net.neoforged.fml.ModList.get().isLoaded("chromeware")) {
            dev.qur1s.spellclasses.implant.ImplantBackpackItem.register(modBus);
            dev.qur1s.spellclasses.implant.KSlots.register(modBus);
            dev.qur1s.spellclasses.implant.GauntletImplants.register(modBus);
            dev.qur1s.spellclasses.implant.ImmunosuppressantTimes.apply();
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(dev.qur1s.spellclasses.implant.BerserkHumanity.class);
        }
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && net.neoforged.fml.ModList.get().isLoaded("create")
                && net.neoforged.fml.ModList.get().isLoaded("chromeware")) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(dev.qur1s.spellclasses.client.HologramSchematics.class);
        }
        if (net.neoforged.fml.ModList.get().isLoaded("supplementaries")) dev.qur1s.spellclasses.block.SconceButtons.register(modBus);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && net.neoforged.fml.ModList.get().isLoaded("create_wizardry")) {
            ManaSiphonGuard.registerPartialsEarly();
        }
        container.registerConfig(ModConfig.Type.SERVER, ClassConfig.SPEC);
        modBus.addListener((ModConfigEvent event) -> {
            if (event.getConfig().getSpec() == ClassConfig.SPEC) ClassManager.invalidateLimitsCache();
        });
        modBus.addListener(ChargedBuffTweaks::onCommonSetup);
        dev.qur1s.spellclasses.compat.CinematicAnvils.register(modBus);
        if (net.neoforged.fml.ModList.get().isLoaded("create") && net.neoforged.fml.ModList.get().isLoaded("cataclysm_spellbooks")) {
            modBus.addListener(dev.qur1s.spellclasses.compat.EngineerHoodGoggles::onCommonSetup);
        }
    }
}
