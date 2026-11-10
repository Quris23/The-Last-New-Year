package lny.octotilt.gun.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.sound.SoundPlayManager;
import lny.octotilt.gun.GunImplants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** The client's part of an implant's gun sliding back into the arm */
public final class GunImplantClient {
   /** How long the put-away animation of the implant guns runs, in milliseconds (their put_away_time) */
   public static final long PUT_AWAY_MS = 700L;

   private GunImplantClient() {
   }

   /** Plays the put-away animation of the gun in hand, as TaCZ does when another gun is drawn after it */
   public static void putAway() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }
      ItemStack gun = player.getMainHandItem();
      if (!GunImplants.isImplantGun(gun)) {
         return;
      }
      // AnimateGeoItemRenderer.tryExit, by reflection: its class extends one of a library bundled inside TaCZ's jar
      Object renderer = IClientItemExtensions.of(gun.getItem()).getCustomRenderer();
      try {
         renderer.getClass().getMethod("tryExit", ItemStack.class, long.class).invoke(renderer, gun, PUT_AWAY_MS);
      } catch (ReflectiveOperationException | RuntimeException e) {
         return;
      }
      TimelessAPI.getGunDisplay(gun).ifPresent(display -> {
         SoundPlayManager.stopPlayGunSound();
         SoundPlayManager.playPutAwaySound(player, display);
      });
   }
}
