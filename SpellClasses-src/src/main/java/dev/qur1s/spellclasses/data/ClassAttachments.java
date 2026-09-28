package dev.qur1s.spellclasses.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public final class ClassAttachments {
    private ClassAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "spellclasses");

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerClassData>> PLAYER_CLASS =
            ATTACHMENT_TYPES.register("player_class", () -> AttachmentType.builder(() -> PlayerClassData.DEFAULT)
                    .serialize(PlayerClassData.CODEC)
                    .sync(PlayerClassData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /** Game-time (in ticks) at which the Laplace Ring may trigger again; 0 = ready. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LAPLACE_COOLDOWN_UNTIL =
            ATTACHMENT_TYPES.register("laplace_cooldown_until", () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .copyOnDeath()
                    .build());

    /** This player's own FTB Quests progress for the class-gated chapters - see {@link PersonalQuestData}. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PersonalQuestData>> PERSONAL_QUEST_PROGRESS =
            ATTACHMENT_TYPES.register("personal_quest_progress", () -> AttachmentType.builder(() -> PersonalQuestData.DEFAULT)
                    .serialize(PersonalQuestData.CODEC)
                    .sync(PersonalQuestData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    /**
     * String forms of the item ids (see {@link dev.qur1s.spellclasses.FtbQuestsUniversalGate}) this
     * player has obtained at least once - unlocks the matching universal chapter's visibility.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Set<String>>> OBTAINED_UNIVERSAL_ITEMS =
            ATTACHMENT_TYPES.register("obtained_universal_items", () -> AttachmentType.<Set<String>>builder(() -> new HashSet<>())
                    .serialize(Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), java.util.ArrayList::new))
                    .sync(ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8))
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
