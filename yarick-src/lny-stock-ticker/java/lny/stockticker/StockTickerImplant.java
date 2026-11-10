package lny.stockticker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Create: Mobile Packages' portable stock ticker worn in a hands socket of Chromeware (let in by SpellClasses, as it lets in
 * the night vision goggles on the face) and opened with the key that deploys the arm hardware: alone in the hands sockets
 * the key opens it, next to hardware in the wrists only with the crouch key held (whatever key the player bound to it, held
 * in any state: flying, swimming, riding). Its own key opens it from the socket too. None of these mods
 * is compiled against: they are reached through reflection, and nothing happens without them.
 */
public final class StockTickerImplant {
   public static final ResourceLocation TICKER = ResourceLocation.fromNamespaceAndPath("create_mobile_packages", "portable_stock_ticker");
   // Chromeware's HANDS category
   public static final int HANDS = 6;
   // Implant capacity it takes up, as SpellClasses' smaller upgrades
   private static final int CAPACITY = 4;
   // The ticker's menus: its own and the trash one opened from it
   private static final String MENUS = "de.theidler.create_mobile_packages.items.portable_stock_ticker.";
   private static boolean looked;
   private static Method implants;
   private static Method slots;
   private static Method slot;
   private static Method item;
   private static Constructor<?> rule;
   private static Method deployable;
   private static Method armItems;
   private static Method open;
   private static Object openPacket;
   // The crouch key as the client last pressed the arm hardware key (StockTickerNetwork.CrouchKey), taken by that key's toggle
   private static final Map<UUID, Boolean> CROUCH_KEY = new ConcurrentHashMap<>();

   private StockTickerImplant() {
   }

   public static boolean isTicker(ItemStack stack) {
      return !stack.isEmpty() && TICKER.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
   }

   /** SpellClasses' installing rule for the ticker: the hands category */
   public static Object rule() {
      look();
      try {
         return rule != null ? rule.newInstance(HANDS, CAPACITY) : null;
      } catch (ReflectiveOperationException | RuntimeException e) {
         return null;
      }
   }

   /** The ticker in a hands socket, or empty */
   public static ItemStack find(Player player) {
      for (ItemStack stack : handsImplants(player)) {
         if (isTicker(stack)) {
            return stack;
         }
      }
      return ItemStack.EMPTY;
   }

   /** On the arm hardware key: whether the ticker was opened instead of the hardware */
   public static boolean openInstead(Player player) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      }
      Boolean crouchKey = CROUCH_KEY.remove(player.getUUID());
      List<ItemStack> hands = handsImplants(player);
      if (hands.stream().noneMatch(StockTickerImplant::isTicker)) {
         return false;
      }
      // Without word from the client, the shared sneaking flag, which follows the same key but not in every state
      boolean crouching = crouchKey != null ? crouchKey : player.isShiftKeyDown();
      if (hasArmHardware(player) && !crouching || open == null) {
         return false;
      }
      // The key belongs to Chromeware, so StockTickerDeploy is there to slide the ticker out of a wrist first
      return StockTickerDeploy.start(serverPlayer) || open(serverPlayer);
   }

   public static void crouchKey(Player player, boolean held) {
      CROUCH_KEY.put(player.getUUID(), held);
   }

   /** Opens the ticker's menu, as its own key does */
   public static boolean open(ServerPlayer player) {
      look();
      if (open == null) {
         return false;
      }
      try {
         open.invoke(openPacket, player);
         return true;
      } catch (ReflectiveOperationException | RuntimeException e) {
         return false;
      }
   }

   public static boolean isMenu(AbstractContainerMenu menu) {
      return menu != null && menu.getClass().getName().startsWith(MENUS);
   }

   // What the key deploys sits in the wrists (Chromeware's arm implants), not in the hands sockets
   private static boolean hasArmHardware(Player player) {
      if (deployable == null) {
         return false;
      }
      try {
         return (Boolean)deployable.invoke(null, player) || !((List<?>)armItems.invoke(null, player)).isEmpty();
      } catch (ReflectiveOperationException | RuntimeException e) {
         return false;
      }
   }

   private static List<ItemStack> handsImplants(Player player) {
      look();
      if (implants == null) {
         return List.of();
      }
      try {
         Object sockets = implants.invoke(null, player, HANDS);
         int count = ((List<?>)slots.invoke(sockets)).size();
         List<ItemStack> stacks = new java.util.ArrayList<>(count);
         for (int i = 0; i < count; i++) {
            Object installed = slot.invoke(sockets, i);
            if (installed != null) {
               ItemStack stack = (ItemStack)item.invoke(installed);
               if (stack != null && !stack.isEmpty()) {
                  stacks.add(stack);
               }
            }
         }
         return stacks;
      } catch (ReflectiveOperationException | RuntimeException e) {
         return List.of();
      }
   }

   private static void look() {
      if (looked) {
         return;
      }
      looked = true;
      try {
         Class<?> sockets = Class.forName("com.chromeware.cyberware.CyberwareSlots");
         implants = Class.forName("com.chromeware.cyberware.InventoryImplants").getMethod("get", Player.class, int.class);
         slots = sockets.getMethod("slots");
         slot = sockets.getMethod("get", int.class);
         item = Class.forName("com.chromeware.cyberware.InstalledImplant").getMethod("item");
      } catch (ReflectiveOperationException | LinkageError e) {
         implants = null;
      }
      try {
         deployable = Class.forName("com.chromeware.cyberware.ArmControls").getMethod("deployable", Player.class);
         armItems = Class.forName("com.chromeware.cyberware.ArmImplants").getMethod("items", Player.class);
      } catch (ReflectiveOperationException | LinkageError e) {
         deployable = null;
      }
      try {
         rule = Class.forName("dev.qur1s.spellclasses.implant.ImplantUpgrades$Rule").getConstructor(int.class, int.class);
      } catch (ReflectiveOperationException | LinkageError e) {
         rule = null;
      }
      try {
         Class<?> packet = Class.forName("de.theidler.create_mobile_packages.items.portable_stock_ticker.OpenPortableStockTicker");
         openPacket = packet.getField("INSTANCE").get(null);
         open = packet.getMethod("handle", ServerPlayer.class);
      } catch (ReflectiveOperationException | LinkageError e) {
         open = null;
      }
   }
}
