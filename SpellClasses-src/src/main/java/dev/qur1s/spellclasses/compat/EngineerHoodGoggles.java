package dev.qur1s.spellclasses.compat;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * The Engineer's Hood (Cataclysm: Spellbooks) counts as Create's Engineer's Goggles: while it is worn the player sees
 * the same stress/speed/capacity overlay. Create has no data-driven way to do this, only the wearing predicate below.
 */
public final class EngineerHoodGoggles {
    private static final ResourceLocation HOOD = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "engineer_hood");

    private EngineerHoodGoggles() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> GogglesItem.addIsWearingPredicate(player -> {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            return !head.isEmpty() && HOOD.equals(BuiltInRegistries.ITEM.getKey(head.getItem()));
        }));
    }
}
