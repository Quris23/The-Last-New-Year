package dev.qur1s.reviveraltar.data;

import com.mojang.serialization.Codec;
import dev.qur1s.reviveraltar.ReviveAltarMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attached to entities.
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ReviveAltarMod.MODID);

    /**
     * True while this player is a "ghost" (died in a hardcore world and is awaiting revival).
     * Persisted to disk, but deliberately NOT using NeoForge's built-in attachment sync:
     * that path calls getData()/setData() (including while restoring from NBT during
     * Entity.load(), before ServerPlayer#connection exists) which unconditionally tries to
     * push a sync packet and throws an NPE, disconnecting every player on login with
     * "Invalid player data". Ghost state is instead synced manually (see GhostSyncPayload)
     * once the player's connection is guaranteed to exist.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> GHOST =
            REGISTER.register("ghost", () -> AttachmentType.builder(holder -> Boolean.FALSE)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());

    /** True once this player has received the singleplayer-hardcore starter altar kit. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> STARTER_KIT_GIVEN =
            REGISTER.register("starter_kit_given", () -> AttachmentType.builder(holder -> Boolean.FALSE)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());
}
