package dev.qur1s.sphereshields.client;

import dev.qur1s.sphereshields.SphereShields;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Swaps the Shield Generator's block and item models for the Hex Core ones. Done on the baked models
 * rather than with resource-pack overrides, because shield_generators' own assets would otherwise win.
 */
@EventBusSubscriber(modid = SphereShields.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HexCoreClient {
    private static final ResourceLocation GENERATOR = ResourceLocation.fromNamespaceAndPath("shield_generators", "shield_generator");

    public static final ModelResourceLocation CORE_MODEL = standalone("block/hex_core_core");
    private static final ModelResourceLocation SHELL_MODEL = standalone("block/hex_core");
    private static final ModelResourceLocation ITEM_MODEL = standalone("block/hex_core_preview");

    private HexCoreClient() {}

    private static ModelResourceLocation standalone(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(SphereShields.MODID, path));
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(CORE_MODEL);
        event.register(SHELL_MODEL);
        event.register(ITEM_MODEL);
    }

    @SubscribeEvent
    public static void replaceGeneratorModels(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        BakedModel shell = models.get(SHELL_MODEL);
        BakedModel item = models.get(ITEM_MODEL);
        if (shell == null || item == null) return;
        for (ModelResourceLocation key : models.keySet().toArray(new ModelResourceLocation[0])) {
            if (!key.id().equals(GENERATOR)) continue;
            models.put(key, "inventory".equals(key.variant()) ? item : shell);
        }
    }
}
