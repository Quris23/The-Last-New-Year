package dev.qur1s.spellclasses.item;

import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ArmorMaterialRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * Helmet, leggings and boots of the Lightbringer set (Iron's Spellbooks only has the chestplate, {@code paladin_chestplate}).
 * Same material, model and texture as that chestplate; the stat bonuses are set per piece in {@link ArmorItems}.
 */
public class LightbringerArmorItem extends ExtendedArmorItem {
    public LightbringerArmorItem(ArmorItem.Type type, Item.Properties properties, AttributeContainer... attributes) {
        super(ArmorMaterialRegistry.PALADIN, type, properties, attributes);
    }

    @Override
    public GeoArmorRenderer<?> supplyRenderer() {
        return new io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer<>(
                new dev.qur1s.spellclasses.client.LightbringerArmorModel());
    }
}
