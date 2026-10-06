package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * The Tornado (Wind's Spellbooks) normally skips its own caster (and friendly targets) when it picks whom to pull.
 * The owner is set aside for the duration of its tick and put back at the end, so the caster is pulled in like
 * everyone else.
 */
@Mixin(targets = "net.raptorzizi.wind_spellbooks.entity.spells.tornado.TornadoEntity", remap = false)
public abstract class WindTornadoPullsCasterMixin {
    @Unique
    private UUID spellclasses$savedOwnerUuid;
    @Unique
    private Entity spellclasses$savedOwner;
    @Unique
    private boolean spellclasses$ownerSetAside;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void spellclasses$forgetOwner(CallbackInfo ci) {
        ProjectileOwnerAccessor self = (ProjectileOwnerAccessor) this;
        spellclasses$savedOwnerUuid = self.spellclasses$getOwnerUUID();
        spellclasses$savedOwner = self.spellclasses$getCachedOwner();
        spellclasses$ownerSetAside = true;
        self.spellclasses$setOwnerUUID(null);
        self.spellclasses$setCachedOwner(null);
    }

    @Inject(method = "tick", at = @At("RETURN"), require = 0)
    private void spellclasses$restoreOwner(CallbackInfo ci) {
        if (!spellclasses$ownerSetAside) return;
        ProjectileOwnerAccessor self = (ProjectileOwnerAccessor) this;
        self.spellclasses$setOwnerUUID(spellclasses$savedOwnerUuid);
        self.spellclasses$setCachedOwner(spellclasses$savedOwner);
        spellclasses$ownerSetAside = false;
    }
}
