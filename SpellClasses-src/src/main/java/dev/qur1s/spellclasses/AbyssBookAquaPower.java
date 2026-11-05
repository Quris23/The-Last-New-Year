package dev.qur1s.spellclasses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * The Book of R'lyeh (Abyss spell book) gives Aqua spell power: 10% more than the Aquamancer's own book (which gives
 * +10%), so +20% here, on top of its Abyssal buff.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class AbyssBookAquaPower {
    private AbyssBookAquaPower() {
    }

    private static final ResourceLocation ABYSS_BOOK = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyss_spell_book");
    private static final ResourceLocation AQUA_POWER = ResourceLocation.fromNamespaceAndPath("somakespells", "aqua_spell_power");
    private static final double AMOUNT = 0.20;

    @SubscribeEvent
    static void onAttributeModifier(CurioAttributeModifierEvent event) {
        if (!ABYSS_BOOK.equals(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) return;
        BuiltInRegistries.ATTRIBUTE.getHolder(AQUA_POWER).ifPresent(aqua -> event.addModifier(aqua, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("spellclasses", "abyss_book_aqua_power"),
                AMOUNT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    }
}
