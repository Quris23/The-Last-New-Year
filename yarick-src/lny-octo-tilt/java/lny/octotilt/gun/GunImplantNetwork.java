package lny.octotilt.gun;

import io.netty.buffer.ByteBuf;
import lny.octotilt.OctoTiltMod;
import lny.octotilt.gun.client.GunImplantClient;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The gun implants' one packet, handled on the main thread */
public final class GunImplantNetwork {
   private GunImplantNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      event.registrar("1").optional().playToClient(PutAway.TYPE, PutAway.CODEC, (payload, context) -> {
         if (FMLEnvironment.dist.isClient()) {
            GunImplantClient.putAway();
         }
      });
   }

   /** The gun in the player's hand starts sliding back into the arm: its put-away animation plays, then the server takes it */
   public record PutAway() implements CustomPacketPayload {
      public static final PutAway INSTANCE = new PutAway();
      public static final Type<PutAway> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OctoTiltMod.MOD_ID, "gun_put_away"));
      public static final StreamCodec<ByteBuf, PutAway> CODEC = StreamCodec.unit(INSTANCE);

      @Override
      public Type<PutAway> type() {
         return TYPE;
      }
   }
}
