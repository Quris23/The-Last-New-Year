package lny.octotilt.gun;

import java.util.List;
import lny.octotilt.OctoTiltMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Create Armorer's guns as Chromeware arm implants. The implant is an item of this mod, fitted into an arm like Chromeware's
 * own arm hardware (mixins in mixin/gun). The key that deploys arm hardware slides the gun out: a TaCZ gun of the pack
 * {@code lny_gun_implants} (model and animations of the arm with the gun coming out of it) is put into the selected hotbar
 * slot, and taken back when the key is pressed again or the slot is left ({@link GunImplantDeploy}). Does nothing without
 * Chromeware and TaCZ.
 */
public final class GunImplants {
   /** The implant's name in Chromeware's implant colours ({@code crane_implant:<part>}) and the item's path */
   public static final String CRANE = "crane_implant";
   /** The TaCZ gun the implant deploys */
   public static final ResourceLocation CRANE_GUN = ResourceLocation.fromNamespaceAndPath("lny_gun_implants", "crane");
   /** The parts of the gun that can be recoloured on Chromeware's customize page, in the order shown */
   public static final List<String> PARTS = List.of("receiver", "handguard", "barrel", "sights", "handle", "lights", "hull", "fittings");
   // custom_data of the implant fitted into an arm
   static final String OUT = "LnyOut";
   static final String SLOT = "LnySlot";
   static final String HELD = "LnyHeld";
   static final String GUN = "LnyGun";
   /** custom_data flag of the gun stack that belongs to an implant */
   static final String MARK = "LnyImplantGun";

   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OctoTiltMod.MOD_ID);
   public static final DeferredItem<Item> CRANE_IMPLANT = ITEMS.register(CRANE, () -> new Item(new Item.Properties().stacksTo(1)) {
      @Override
      public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
         tooltip.add(Component.translatable("item.lny_octo_tilt.crane_implant.slot").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.translatable("item.lny_octo_tilt.crane_implant.use").withStyle(ChatFormatting.DARK_GRAY));
      }
   });

   public static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, OctoTiltMod.MOD_ID);
   /** Recipe ingredient: a TaCZ gun by its gun id (the implant is made of the gun itself) */
   public static final DeferredHolder<IngredientType<?>, IngredientType<TaczGunIngredient>> TACZ_GUN = INGREDIENTS.register(
      "tacz_gun", () -> new IngredientType<>(TaczGunIngredient.CODEC)
   );

   private GunImplants() {
   }

   public static boolean active() {
      return ModList.get().isLoaded("chromeware") && ModList.get().isLoaded("tacz");
   }

   public static void register(IEventBus modBus) {
      ITEMS.register(modBus);
      INGREDIENTS.register(modBus);
      modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
         if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CRANE_IMPLANT.get());
         }
      });
   }

   private static List<String> allParts;

   /** Chromeware's part names with the gun implant's own after them */
   public static List<String> withGunParts(List<String> parts) {
      if (allParts == null) {
         List<String> all = new java.util.ArrayList<>(parts);
         for (String part : PARTS) {
            if (!all.contains(part)) {
               all.add(part);
            }
         }
         allParts = List.copyOf(all);
      }
      return allParts;
   }

   public static boolean isImplant(ItemStack stack) {
      return !stack.isEmpty() && stack.is(CRANE_IMPLANT.get());
   }

   /** Whether the stack is a gun that belongs to an implant (it lives only in the hand of the player it slid out of) */
   public static boolean isImplantGun(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data != null && data.contains(MARK);
   }

   static CompoundTag data(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? new CompoundTag() : data.copyTag();
   }

   static void data(ItemStack stack, CompoundTag tag) {
      if (tag.isEmpty()) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }
}
