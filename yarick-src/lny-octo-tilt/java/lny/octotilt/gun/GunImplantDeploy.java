package lny.octotilt.gun;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplant;
import com.chromeware.cyberware.ArmImplants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The gun of an implant sliding out of the arm and back, on the server.
 * <p>
 * Out: the gun (kept inside the fitted implant between uses, with its ammunition and attachments) goes into the selected
 * hotbar slot, flagged as an implant's gun; what was held there is kept inside the implant meanwhile. TaCZ then treats it as
 * any gun in the hand: its draw animation is the gun coming out of the forearm.
 * <p>
 * Back in: on the key, on leaving the slot, on death, on leaving the game, when the gun is thrown or moved anywhere else,
 * and when Chromeware puts all arm hardware away. The gun returns into the implant and the held item into its slot. A
 * flagged gun anywhere but the hand it slid out of is removed, so the gun cannot be taken out of the arm as an item.
 */
public final class GunImplantDeploy {
   // Ticks the put-away animation is given before the gun is taken from the hand (put_away_time of the implant guns)
   private static final int PUT_AWAY_TICKS = 14;
   // Players whose gun is sliding back in on the key, with the ticks left
   private static final java.util.Map<java.util.UUID, Integer> PUTTING_AWAY = new java.util.HashMap<>();
   private GunImplantDeploy() {
   }

   public static void register() {
      NeoForge.EVENT_BUS.addListener(GunImplantDeploy::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, GunImplantDeploy::onDeath);
      NeoForge.EVENT_BUS.addListener(GunImplantDeploy::onLogout);
      NeoForge.EVENT_BUS.addListener(GunImplantDeploy::onToss);
      NeoForge.EVENT_BUS.addListener(GunImplantDeploy::onItemEntity);
   }

   /** An implant about to be fitted starts put away: what a copy taken out mid-use still says of the hand is dropped */
   public static void clean(ItemStack implant) {
      if (GunImplants.isImplant(implant)) {
         CompoundTag tag = GunImplants.data(implant);
         tag.remove(GunImplants.OUT);
         tag.remove(GunImplants.SLOT);
         tag.remove(GunImplants.HELD);
         GunImplants.data(implant, tag);
      }
   }

   /** The arm the implant is fitted into (the main arm first), or null */
   public static HumanoidArm side(Player player) {
      HumanoidArm main = player.getMainArm();
      if (ArmImplants.has(player, main, GunImplants::isImplant)) {
         return main;
      }
      return ArmImplants.has(player, main.getOpposite(), GunImplants::isImplant) ? main.getOpposite() : null;
   }

   public static boolean installed(Player player) {
      return side(player) != null;
   }

   /** The arm whose gun is out, or null */
   public static HumanoidArm out(Player player) {
      for (HumanoidArm side : HumanoidArm.values()) {
         ArmImplant implant = ArmImplants.get(player, side);
         if (implant.is(GunImplants::isImplant) && GunImplants.data(implant.item()).getBoolean(GunImplants.OUT)) {
            return side;
         }
      }
      return null;
   }

   /** On the arm hardware key with no hardware out: whether the gun slid out (or the key was used up by the attempt) */
   public static boolean deploy(ServerPlayer player) {
      HumanoidArm side = side(player);
      if (side == null) {
         return false;
      }
      if (!usable(player, side)) {
         player.displayClientMessage(Component.translatable("message.lny_octo_tilt.gun_implant.arm"), true);
         return true;
      }
      Inventory inventory = player.getInventory();
      int slot = inventory.selected;
      ItemStack implant = ArmImplants.get(player, side).item();
      CompoundTag tag = GunImplants.data(implant);
      ItemStack gun = tag.contains(GunImplants.GUN) ? ItemStack.parseOptional(player.registryAccess(), tag.getCompound(GunImplants.GUN)) : ItemStack.EMPTY;
      if (gun.isEmpty() || !TaczGuns.is(gun, GunImplants.CRANE_GUN)) {
         gun = TaczGuns.create(GunImplants.CRANE_GUN, player.registryAccess());
      }
      if (gun.isEmpty()) {
         return false;
      }
      CompoundTag gunTag = GunImplants.data(gun);
      gunTag.putBoolean(GunImplants.MARK, true);
      GunImplants.data(gun, gunTag);

      ItemStack held = inventory.getItem(slot);
      tag.remove(GunImplants.GUN);
      if (held.isEmpty()) {
         tag.remove(GunImplants.HELD);
      } else {
         tag.put(GunImplants.HELD, held.save(player.registryAccess()));
      }
      tag.putInt(GunImplants.SLOT, slot);
      tag.putBoolean(GunImplants.OUT, true);
      GunImplants.data(implant, tag);
      ArmImplants.set(player, side, new ArmImplant(implant, side));
      inventory.setItem(slot, gun);
      inventory.setChanged();
      player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.35F, 1.3F);
      return true;
   }

   /** On the arm hardware key with the gun out: it slides back in, its put-away animation first */
   public static void putAway(ServerPlayer player) {
      if (PUTTING_AWAY.putIfAbsent(player.getUUID(), PUT_AWAY_TICKS) == null) {
         net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, GunImplantNetwork.PutAway.INSTANCE);
      }
   }

   /** Puts every implant's gun back in; whether one was out */
   public static boolean retractAll(Player player) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      }
      PUTTING_AWAY.remove(player.getUUID());
      boolean any = false;
      for (HumanoidArm side : HumanoidArm.values()) {
         any |= retract(serverPlayer, side, ItemStack.EMPTY);
      }
      return any;
   }

   // found: the gun when the caller already holds it (thrown), else it is looked for
   private static boolean retract(ServerPlayer player, HumanoidArm side, ItemStack found) {
      ArmImplant fitted = ArmImplants.get(player, side);
      if (!fitted.is(GunImplants::isImplant)) {
         return false;
      }
      ItemStack implant = fitted.item();
      CompoundTag tag = GunImplants.data(implant);
      if (!tag.getBoolean(GunImplants.OUT)) {
         return false;
      }
      ItemStack gun = found.isEmpty() ? take(player) : found;
      if (!gun.isEmpty()) {
         CompoundTag gunTag = GunImplants.data(gun);
         gunTag.remove(GunImplants.MARK);
         GunImplants.data(gun, gunTag);
         tag.put(GunImplants.GUN, gun.save(player.registryAccess()));
      }
      ItemStack held = tag.contains(GunImplants.HELD) ? ItemStack.parseOptional(player.registryAccess(), tag.getCompound(GunImplants.HELD)) : ItemStack.EMPTY;
      int slot = tag.getInt(GunImplants.SLOT);
      tag.remove(GunImplants.HELD);
      tag.remove(GunImplants.SLOT);
      tag.remove(GunImplants.OUT);
      GunImplants.data(implant, tag);
      ArmImplants.set(player, side, new ArmImplant(implant, side));
      if (!held.isEmpty()) {
         Inventory inventory = player.getInventory();
         if (Inventory.isHotbarSlot(slot) && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, held);
         } else if (!inventory.add(held)) {
            player.drop(held, false);
         }
         inventory.setChanged();
      }
      player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.3F, 1.3F);
      return true;
   }

   // The flagged gun, removed from wherever the player has it: the inventory, the cursor, the open container
   private static ItemStack take(ServerPlayer player) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (GunImplants.isImplantGun(stack)) {
            inventory.setItem(i, ItemStack.EMPTY);
            return stack;
         }
      }
      ItemStack carried = player.containerMenu.getCarried();
      if (GunImplants.isImplantGun(carried)) {
         player.containerMenu.setCarried(ItemStack.EMPTY);
         return carried;
      }
      for (Slot slot : player.containerMenu.slots) {
         ItemStack stack = slot.getItem();
         if (GunImplants.isImplantGun(stack)) {
            slot.set(ItemStack.EMPTY);
            return stack;
         }
      }
      return ItemStack.EMPTY;
   }

   private static boolean usable(Player player, HumanoidArm side) {
      if (!player.isAlive() || player.isSpectator() || !ArmHardware.free(player, side)) {
         return false;
      }
      LimbState body = LimbAttachments.get(player);
      return !body.isMissing(BodyPart.BODY) && !body.isMissing(ArmImplants.part(side)) && !body.isMissing(ArmImplants.handPart(side));
   }

   private static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      Integer left = PUTTING_AWAY.get(player.getUUID());
      if (left != null) {
         if (left <= 1 || out(player) == null) {
            retractAll(player);
         } else {
            PUTTING_AWAY.put(player.getUUID(), left - 1);
         }
      }
      Inventory inventory = player.getInventory();
      int validSlot = -1;
      for (HumanoidArm side : HumanoidArm.values()) {
         ArmImplant fitted = ArmImplants.get(player, side);
         if (!fitted.is(GunImplants::isImplant)) {
            continue;
         }
         CompoundTag tag = GunImplants.data(fitted.item());
         if (!tag.getBoolean(GunImplants.OUT)) {
            continue;
         }
         int slot = tag.getInt(GunImplants.SLOT);
         boolean valid = validSlot < 0 && inventory.selected == slot && Inventory.isHotbarSlot(slot) && GunImplants.isImplantGun(inventory.getItem(slot)) && usable(player, side);
         if (valid) {
            validSlot = slot;
         } else {
            PUTTING_AWAY.remove(player.getUUID());
            retract(player, side, ItemStack.EMPTY);
         }
      }
      // a flagged gun with no implant behind it (taken from a container, left over from a removed implant)
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (i != validSlot && GunImplants.isImplantGun(inventory.getItem(i))) {
            inventory.setItem(i, ItemStack.EMPTY);
         }
      }
   }

   private static void onDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         retractAll(player);
      }
   }

   private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      retractAll(event.getEntity());
   }

   private static void onToss(ItemTossEvent event) {
      ItemStack stack = event.getEntity().getItem();
      if (!GunImplants.isImplantGun(stack)) {
         return;
      }
      event.setCanceled(true);
      if (event.getPlayer() instanceof ServerPlayer player) {
         HumanoidArm side = out(player);
         if (side != null) {
            retract(player, side, stack.copy());
         }
      }
   }

   // however it got there, an implant's gun does not lie in the world
   private static void onItemEntity(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity item && GunImplants.isImplantGun(item.getItem())) {
         event.setCanceled(true);
      }
   }
}
