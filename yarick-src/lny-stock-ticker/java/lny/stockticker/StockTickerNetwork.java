package lny.stockticker;

import io.netty.buffer.ByteBuf;
import lny.stockticker.client.StockTickerClient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** The ticker's two packets, both handled on the main thread */
public final class StockTickerNetwork {
   private StockTickerNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToClient(Arm.TYPE, Arm.CODEC, (payload, context) -> StockTickerClient.arm(payload));
      registrar.playToServer(CrouchKey.TYPE, CrouchKey.CODEC, (payload, context) -> StockTickerImplant.crouchKey(context.player(), payload.held()));
   }

   /** The portable stock ticker sliding out of a player's wrist or back in (StockTickerDeploy) */
   public record Arm(int id, boolean left, boolean out) implements CustomPacketPayload {
      public static final Type<Arm> TYPE = new Type<>(StockTickerMod.id("arm"));
      public static final StreamCodec<ByteBuf, Arm> CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, Arm::id, ByteBufCodecs.BOOL, Arm::left, ByteBufCodecs.BOOL, Arm::out, Arm::new
      );

      @Override
      public Type<Arm> type() {
         return TYPE;
      }
   }

   /** Whether the player's crouch key was held as Chromeware's arm hardware key was pressed, sent just before that key's own packet */
   public record CrouchKey(boolean held) implements CustomPacketPayload {
      public static final Type<CrouchKey> TYPE = new Type<>(StockTickerMod.id("crouch_key"));
      public static final StreamCodec<ByteBuf, CrouchKey> CODEC = ByteBufCodecs.BOOL.map(CrouchKey::new, CrouchKey::held);

      @Override
      public Type<CrouchKey> type() {
         return TYPE;
      }
   }
}
