package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Overrides just the movement-speed bonus of Iron's Spellbooks' Charged effect (from the Overload
 * spell) to 25%/level instead of the stock 5%/level. {@code addAttributeModifier} keys its internal
 * map by attribute, so calling it again for the same attribute simply replaces the stock entry.
 */
public final class ChargedBuffTweaks {
    private ChargedBuffTweaks() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> MobEffectRegistry.CHARGED.get().addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "mobeffect_charged"),
                0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
