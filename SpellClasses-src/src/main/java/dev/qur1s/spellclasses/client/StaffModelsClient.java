package dev.qur1s.spellclasses.client;

import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Replaces the models of three Iron's Spells 'n Spellbooks staffs with our own ones. Done on the baked models (swapped when the
 * models finish baking), so it wins over the mod's own assets and over any resource pack:
 * <ul>
 *   <li>{@code lightning_rod} ("Жезл молний") - copper / oxidized-copper fittings, wind charge with lightning sparks;</li>
 *   <li>{@code artificer_cane} ("Трость изобретателя") - meshed animated cogs, leather-wrapped grip, faceted crystal, rare lightning;</li>
 *   <li>{@code graybeard_staff} ("Посох старца") - wooden staff for a priest: cloth + rope bindings, ribbons, animated fireflies.</li>
 * </ul>
 * Does nothing for an item when Iron's Spells isn't installed - its key is simply never found.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StaffModelsClient {
    /** item id in irons_spellbooks -> path of our replacement model in this mod's namespace. */
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "lightning_rod", "item/storm_rod",
            "artificer_cane", "item/artificer_cane",
            "graybeard_staff", "item/graybeard_staff");

    private StaffModelsClient() {
    }

    private static ModelResourceLocation ours(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("spellclasses", path));
    }

    private static ModelResourceLocation theirs(String item) {
        return ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", item));
    }

    @SubscribeEvent
    static void registerModels(ModelEvent.RegisterAdditional event) {
        REPLACEMENTS.values().forEach(path -> event.register(ours(path)));
    }

    @SubscribeEvent
    static void replaceModels(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        REPLACEMENTS.forEach((item, path) -> {
            BakedModel replacement = models.get(ours(path));
            if (replacement != null && models.containsKey(theirs(item))) models.put(theirs(item), replacement);
        });
    }
}
