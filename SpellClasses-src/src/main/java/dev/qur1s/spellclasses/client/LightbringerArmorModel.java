package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.item.LightbringerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

/** Geometry and texture of the Lightbringer armor (the whole set shares one model; the renderer shows the bones of each piece's slot). */
public class LightbringerArmorModel extends DefaultedItemGeoModel<LightbringerArmorItem> {
    public LightbringerArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath("spellclasses", "lightbringer_armor"));
    }

    @Override
    public ResourceLocation getModelResource(LightbringerArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("spellclasses", "geo/lightbringer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LightbringerArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/models/armor/lightbringer_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LightbringerArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
    }
}
