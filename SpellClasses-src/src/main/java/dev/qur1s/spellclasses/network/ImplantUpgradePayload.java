package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: a button next to the inventory was clicked (the implant socket it belongs to). {@code action}: 0 on/off, 1 receive/give, 2 world, 3 storages, 4 tank in hand (pump settings). */
public record ImplantUpgradePayload(int category, int socket, int action) implements CustomPacketPayload {
    public static final Type<ImplantUpgradePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "implant_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImplantUpgradePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ImplantUpgradePayload::category,
            ByteBufCodecs.VAR_INT, ImplantUpgradePayload::socket,
            ByteBufCodecs.VAR_INT, ImplantUpgradePayload::action,
            ImplantUpgradePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
