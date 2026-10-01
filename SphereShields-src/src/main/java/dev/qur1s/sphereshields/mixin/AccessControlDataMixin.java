package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.AllowedPlayersHolder;
import dev.qur1s.sphereshields.MobAccessHolder;
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
 * Adds a per-generator whitelist of player UUIDs, plus a flat "allow all mobs" toggle, directly onto
 * {@code ShieldGeneratorBlockEntity} - saved/loaded/synced the same way the mod's own fields already
 * are (saveAdditional / loadAdditional / getUpdateTag), so both survive restarts and reach the client
 * for the access GUI. Any player NOT on the whitelist gets pushed out of the dome exactly like a
 * hostile mob (see HostileMobBarrier), unless the mob toggle is on, in which case mobs are left alone
 * entirely (the player whitelist still applies to players regardless).
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
public abstract class AccessControlDataMixin implements AllowedPlayersHolder, MobAccessHolder {
    private static final String ALLOWED_PLAYERS_KEY = "SphereShields_AllowedPlayers";
    private static final String ALLOW_MOBS_KEY = "SphereShields_AllowMobs";

    @Unique
    private final Set<UUID> sphereshields$allowedPlayers = new LinkedHashSet<>();

    @Unique
    private boolean sphereshields$allowMobs = false;

    @Override
    public Set<UUID> sphereshields$getAllowedPlayers() {
        return this.sphereshields$allowedPlayers;
    }

    @Override
    public void sphereshields$setAllowedPlayers(Set<UUID> allowed) {
        this.sphereshields$allowedPlayers.clear();
        this.sphereshields$allowedPlayers.addAll(allowed);
    }

    @Override
    public boolean sphereshields$isAllowMobs() {
        return this.sphereshields$allowMobs;
    }

    @Override
    public void sphereshields$setAllowMobs(boolean allow) {
        this.sphereshields$allowMobs = allow;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void sphereshields$saveAllowed(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ListTag list = new ListTag();
        for (UUID id : this.sphereshields$allowedPlayers) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put(ALLOWED_PLAYERS_KEY, list);
        tag.putBoolean(ALLOW_MOBS_KEY, this.sphereshields$allowMobs);
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
        this.sphereshields$allowMobs = tag.getBoolean(ALLOW_MOBS_KEY);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void sphereshields$includeAllowedInUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        ListTag list = new ListTag();
        for (UUID id : this.sphereshields$allowedPlayers) {
            list.add(StringTag.valueOf(id.toString()));
        }
        cir.getReturnValue().put(ALLOWED_PLAYERS_KEY, list);
        cir.getReturnValue().putBoolean(ALLOW_MOBS_KEY, this.sphereshields$allowMobs);
    }
}
