package lny.octotilt;

import net.minecraft.world.entity.player.Player;
import lny.octotilt.gun.GunImplantDeploy;
import lny.octotilt.gun.GunImplantNetwork;
import lny.octotilt.gun.GunImplants;
import lny.octotilt.gun.client.GunImplantColors;
import lny.octotilt.rig.RigReload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Quaternionf;

/**
 * While the Create: Octo Arms tentacle rig carries the player, they lean to the surfaces its tentacles grip
 * ({@link OctoTilt}, client side): the camera through Aeronautics Camera Sync's tilt sources, the body and hitbox through
 * aero_player_tilt (mixin), by the same rotation. On the server (the integrated one too) it keeps the rig from being stowed
 * by its speed check right after a server stall ({@link ServerLagGrace}). On both sides, a carried player digs as fast as
 * one standing ({@link RigMining}). It also draws the rig implanted through Spell Classes (package rig): on the back, in
 * the colours of a single Chromeware row for both tiers, in first person where the body is. It also holds the gun implants
 * (package gun): Create Armorer's guns sliding out of a Chromeware arm; their item needs the mod on both sides.
 */
@Mod(OctoTiltMod.MOD_ID)
public final class OctoTiltMod {
   public static final String MOD_ID = "lny_octo_tilt";
   static final boolean BODY = ModList.get().isLoaded("aero_player_tilt");

   // The lean aero_player_tilt draws the player's body with, null when there is none
   public static Quaternionf bodyLean(Player player, float partialTick) {
      return BODY ? AptBody.lean(player, partialTick) : null;
   }

   public OctoTiltMod(IEventBus modBus, Dist dist) {
      // the gun implants (package gun): the item always, so both sides agree on it; the rest with Chromeware and TaCZ
      GunImplants.register(modBus);
      modBus.addListener(GunImplantNetwork::register);
      if (GunImplants.active()) {
         GunImplantDeploy.register();
         if (dist.isClient()) {
            GunImplantColors.register(modBus);
         }
      }
      if (!ModList.get().isLoaded("tentaworks")) {
         return;
      }
      NeoForge.EVENT_BUS.addListener(ServerLagGrace::onServerTick);
      NeoForge.EVENT_BUS.addListener(RigMining::onBreakSpeed);
      if (dist.isClient()) {
         RigReload.register(modBus);
      }
      if (dist.isClient() && ModList.get().isLoaded("aero_cam_sync")) {
         NeoForge.EVENT_BUS.addListener(OctoTilt::onRenderFrame);
         OctoArmsCameraTilt.register();
      }
   }
}
