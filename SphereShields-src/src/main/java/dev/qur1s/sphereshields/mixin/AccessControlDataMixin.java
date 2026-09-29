package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.AllowedPlayersHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Adds a per-generator whitelist of player UUIDs directly onto {@code ShieldGeneratorBlockEntity} -
 * saved/loaded/synced the same way the mod's own fields already are (saveAdditional / loadAdditional
 * / getUpdateTag), so it survives restarts and reaches the client for the access GUI. Any player NOT
 * on this list gets pushed out of the dome exactly like a hostile mob (see HostileMobBarrier).
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
public abstract class AccessControlDataMixin implements AllowedPlayersHolder {
    private static final String ALLOWED_PLAYERS_KEY = "SphereShields_AllowedPlayers";

    @Unique
    private final Set<UUID> sphereshields$allowedPlayers = new LinkedHashSet<>();

    @Override
    public Set<UUID> sphereshields$getAllowedPlayers() {
        return this.sphereshields$allowedPlayers;
    }

    @Override
    public void sphereshields$setAllowedPlayers(Set<UUID> allowed) {
        this.sphereshields$allowedPlayers.clear();
        this.sphereshields$allowedPlayers.addAll(allowed);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void sphereshields$saveAllowed(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ListTag list = new ListTag();
        for (UUID id : this.sphereshields$allowedPlayers) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put(ALLOWED_PLAYERS_KEY, list);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void sphereshields$loadAllowed(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.sphereshields$allowedPlayers.clear();
        if (tag.contains(ALLOWED_PLAYERS_KEY, 9)) {
            for (Tag entry : tag.getList(ALLOWED_PLAYERS_KEY, 8)) {
                try {
                    this.sphereshields$allowedPlayers.add(UUID.fromString(entry.getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void sphereshields$includeAllowedInUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        ListTag list = new ListTag();
        for (UUID id : this.sphereshields$allowedPlayers) {
            list.add(StringTag.valueOf(id.toString()));
        }
        cir.getReturnValue().put(ALLOWED_PLAYERS_KEY, list);
    }
}
