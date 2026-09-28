package dev.qur1s.spellclasses.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/** Attached to every player. {@code chosenSchool} is the id (e.g. "irons_spellbooks:fire") once picked. */
public record PlayerClassData(Optional<String> chosenSchool, boolean bookGiven) {
    public static final PlayerClassData DEFAULT = new PlayerClassData(Optional.empty(), false);

    public static final Codec<PlayerClassData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("chosen_school", "").xmap(
                    s -> s.isEmpty() ? Optional.<String>empty() : Optional.of(s),
                    o -> o.orElse("")
            ).forGetter(PlayerClassData::chosenSchool),
            Codec.BOOL.fieldOf("book_given").orElse(false).forGetter(PlayerClassData::bookGiven)
    ).apply(instance, PlayerClassData::new));

    /**
     * Synced to the owning client so client-side code (FTB Quests' chapter visibility check
     * among others) can see the chosen class - {@link #CODEC} above only persists to disk.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerClassData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PlayerClassData::chosenSchool,
            ByteBufCodecs.BOOL, PlayerClassData::bookGiven,
            PlayerClassData::new
    );

    public PlayerClassData withBookGiven() {
        return new PlayerClassData(chosenSchool, true);
    }

    public PlayerClassData withChosenSchool(String schoolId) {
        return new PlayerClassData(Optional.of(schoolId), true);
    }

    public boolean hasChosen() {
        return chosenSchool.isPresent();
    }
}
