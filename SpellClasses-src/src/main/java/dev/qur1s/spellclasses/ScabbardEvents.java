package dev.qur1s.spellclasses;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

/** Only weapons and staves may go into the scabbard slot. */
@EventBusSubscriber(modid = "spellclasses")
public final class ScabbardEvents {
    private ScabbardEvents() {
    }

    @SubscribeEvent
    static void onCanEquip(CurioCanEquipEvent event) {
        if (!Scabbard.SLOT.equals(event.getSlotContext().identifier())) return;
        event.setEquipResult(Scabbard.accepts(event.getStack()) ? TriState.TRUE : TriState.FALSE);
    }
}
