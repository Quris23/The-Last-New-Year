package lny.octotilt.gun;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * Recipe ingredient {@code lny_octo_tilt:tacz_gun}: a TaCZ gun by its gun id, whatever its ammunition, fire mode and
 * attachments (all of TaCZ's guns are one item, told apart by custom data that a component ingredient would have to match
 * whole). {@code {"type": "lny_octo_tilt:tacz_gun", "gun": "create_armorer:rifle_assult_crane"}}
 */
public record TaczGunIngredient(ResourceLocation gun) implements ICustomIngredient {
   public static final MapCodec<TaczGunIngredient> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(ResourceLocation.CODEC.fieldOf("gun").forGetter(TaczGunIngredient::gun)).apply(instance, TaczGunIngredient::new)
   );
   private static final ResourceLocation GUN_ITEM = ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");

   @Override
   public boolean test(ItemStack stack) {
      return !stack.isEmpty() && ModList.get().isLoaded("tacz") && !GunImplants.isImplantGun(stack) && TaczGuns.is(stack, this.gun);
   }

   // what recipe viewers show
   @Override
   public Stream<ItemStack> getItems() {
      Item item = BuiltInRegistries.ITEM.get(GUN_ITEM);
      if (item == Items.AIR) {
         return Stream.empty();
      }
      ItemStack stack = new ItemStack(item);
      CompoundTag tag = new CompoundTag();
      tag.putString("GunId", this.gun.toString());
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return Stream.of(stack);
   }

   @Override
   public boolean isSimple() {
      return false;
   }

   @Override
   public IngredientType<?> getType() {
      return GunImplants.TACZ_GUN.get();
   }
}
