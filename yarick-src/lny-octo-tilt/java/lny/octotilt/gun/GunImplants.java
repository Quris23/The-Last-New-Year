package lny.octotilt.gun;

import java.util.ArrayList;
import java.util.Collections;
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
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Create Armorer's guns as Chromeware arm implants. An implant is an item of this mod, fitted into an arm like Chromeware's
 * own arm hardware (mixins in mixin/gun). The key that deploys arm hardware slides the gun out: a TaCZ gun of the pack
 * {@code lny_gun_implants} (model and animations of the arm with the gun coming out of it) is put into the selected hotbar
 * slot, and taken back when the key is pressed again or the slot is left ({@link GunImplantDeploy}). Does nothing without
 * Chromeware and TaCZ.
 * <p>
 * A new implant is one more {@link Kind} here plus its resources: the gun pack files under {@code lny_gun_implants}
 * (display, data, index, model, animations, texture, part mask), the item model and texture, the recipe and the names.
 */
public final class GunImplants {
   /** Implant capacity an implant takes up, as Chromeware's own arm weapons do */
   public static final int CAPACITY = 4;
   /** The parts of a gun that can be recoloured on Chromeware's customize page, in the order shown */
   public static final List<String> PARTS = List.of("receiver", "handguard", "barrel", "sights", "handle", "lights", "hull", "fittings");
   // custom_data of the implant fitted into an arm
   static final String OUT = "LnyOut";
   static final String SLOT = "LnySlot";
   static final String HELD = "LnyHeld";
   static final String GUN = "LnyGun";
   /** custom_data flag of the gun stack that belongs to an implant */
   static final String MARK = "LnyImplantGun";

   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OctoTiltMod.MOD_ID);
   private static final List<Kind> KINDS = new ArrayList<>();
   /** The assault rifle "Crane" */
   public static final Kind CRANE = kind("crane_implant", "crane");
   /** The double-barrelled shotgun "Stone" */
   public static final Kind STONE = kind("stone_implant", "stone");

   public static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, OctoTiltMod.MOD_ID);
   /** Recipe ingredient: a TaCZ gun by its gun id */
   public static final DeferredHolder<IngredientType<?>, IngredientType<TaczGunIngredient>> TACZ_GUN = INGREDIENTS.register(
      "tacz_gun", () -> new IngredientType<>(TaczGunIngredient.CODEC)
   );

   /**
    * One gun implant.
    *
    * @param key  the item's path and the implant's name in Chromeware's implant colours ({@code <key>:<part>})
    * @param name the name of its gun in the pack {@code lny_gun_implants}
    */
   public record Kind(String key, String name, DeferredItem<Item> item) {
      /** The TaCZ gun the implant deploys */
      public ResourceLocation gun() {
         return ResourceLocation.fromNamespaceAndPath("lny_gun_implants", this.name);
      }

      public ResourceLocation display() {
         return ResourceLocation.fromNamespaceAndPath("lny_gun_implants", this.name + "_display");
      }

      /** Which pixels of the gun's texture are which part (see GunImplantColors) */
      public ResourceLocation mask() {
         return ResourceLocation.fromNamespaceAndPath("lny_gun_implants", "textures/gun/uv/" + this.name + "_parts.png");
      }
   }

   private GunImplants() {
   }

   private static Kind kind(String key, String name) {
      DeferredItem<Item> item = ITEMS.register(key, () -> new Item(new Item.Properties().stacksTo(1)) {
         // the lines Chromeware puts under its own implants, in its own words
         @Override
         public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("tooltip.chromeware.slot", Component.translatable("screen.chromeware.category.arms")).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.chromeware.capacity", CAPACITY).withStyle(ChatFormatting.AQUA));
         }
      });
      Kind kind = new Kind(key, name, item);
      KINDS.add(kind);
      return kind;
   }

   public static List<Kind> kinds() {
      return Collections.unmodifiableList(KINDS);
   }

   public static boolean active() {
      return ModList.get().isLoaded("chromeware") && ModList.get().isLoaded("tacz");
   }

   public static void register(IEventBus modBus) {
      ITEMS.register(modBus);
      INGREDIENTS.register(modBus);
      modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
         if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (Kind kind : KINDS) {
               event.accept(kind.item().get());
            }
         }
      });
   }

   private static List<String> allParts;

   /** Chromeware's part names with the gun implants' own after them */
   public static List<String> withGunParts(List<String> parts) {
      if (allParts == null) {
         List<String> all = new ArrayList<>(parts);
         for (String part : PARTS) {
            if (!all.contains(part)) {
               all.add(part);
            }
         }
         allParts = List.copyOf(all);
      }
      return allParts;
   }

   /** The implant the stack is, or null */
   public static Kind of(ItemStack stack) {
      if (!stack.isEmpty()) {
         for (Kind kind : KINDS) {
            if (stack.is(kind.item().get())) {
               return kind;
            }
         }
      }
      return null;
   }

   /** The implant by its name in Chromeware's implant colours, or null */
   public static Kind byKey(String key) {
      for (Kind kind : KINDS) {
         if (kind.key().equals(key)) {
            return kind;
         }
      }
      return null;
   }

   /** The implant whose gun the TaCZ gun stack is (by its gun id), or null */
   @SuppressWarnings("deprecation")
   public static Kind ofGun(ItemStack gun) {
      if (gun == null || gun.isEmpty()) {
         return null;
      }
      CustomData data = gun.get(DataComponents.CUSTOM_DATA);
      if (data == null) {
         return null;
      }
      String id = data.getUnsafe().getString("GunId");
      if (id.startsWith("lny_gun_implants:")) {
         for (Kind kind : KINDS) {
            if (id.length() == 17 + kind.name().length() && id.endsWith(kind.name())) {
               return kind;
            }
         }
      }
      return null;
   }

   public static boolean isImplant(ItemStack stack) {
      return of(stack) != null;
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
