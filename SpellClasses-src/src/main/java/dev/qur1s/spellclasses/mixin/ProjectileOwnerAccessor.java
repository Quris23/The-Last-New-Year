package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

/**
 * {@code Projectile.setOwner(null)} is a no-op in vanilla (it only ever sets a non-null owner), so briefly clearing
 * the owner of a projectile needs direct access to the two backing fields.
 */
@Mixin(Projectile.class)
public interface ProjectileOwnerAccessor {
    @Accessor("ownerUUID")
    UUID spellclasses$getOwnerUUID();

    @Accessor("ownerUUID")
    void spellclasses$setOwnerUUID(UUID uuid);

    @Accessor("cachedOwner")
    Entity spellclasses$getCachedOwner();

    @Accessor("cachedOwner")
    void spellclasses$setCachedOwner(Entity entity);
}
