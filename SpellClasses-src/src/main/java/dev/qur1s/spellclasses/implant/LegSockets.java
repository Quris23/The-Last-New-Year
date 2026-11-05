package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.ImplantCategory;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Field;

/** Chromeware stores three sockets for every category and uses only the first few: the Legs get all three. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class LegSockets {
    public static final int COUNT = 3;

    private LegSockets() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Field sockets = ImplantCategory.class.getField("sockets");
                sockets.setAccessible(true);
                sockets.setInt(ImplantCategory.LEGS, COUNT);
                sockets.setInt(ImplantCategory.OPERATING_SYSTEM, 3);
                sockets.setInt(ImplantCategory.FACE, 3);
            } catch (Throwable t) {
                LogUtils.getLogger().error("Cannot raise the number of Legs sockets", t);
            }
        });
    }
}
