package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.spells.fire_arrow.FireArrowProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fire Arrow blows up where it lands with Create Nuclear's reactor explosion (its sound, mushroom cloud and crater),
 * marked as "clean" so it deals no radiation damage (see NuclearBlastNoRadiationMixin). Without Create Nuclear it is a
 * vanilla TNT blast.
 */
@Mixin(value = FireArrowProjectile.class, remap = false)
public abstract class FireArrowImpactMixin {
    private static final ResourceLocation NUCLEAR_EXPLOSION = ResourceLocation.fromNamespaceAndPath("createnuclear", "nuclear_explosion");
    /** Reactor-style blast size: the explosion reaches this many chunks around the impact. */
    private static final float NUCLEAR_SIZE = 1.0f;
    /** The blast entity sits this far above the impact, in the air: Create Nuclear lowers the cloud through solid blocks below it. */
    private static final double BLAST_LIFT = 3.0;
    /** Players this close to the blast get the ringing in the ears. */
    private static final double RINGING_RANGE = 100.0;
    private static final float TNT_POWER = 4.0f;
    private static final int SHAKE_TICKS = 140;
    private static final float SHAKE_RADIUS = 120.0f;

    @Inject(method = "onHit", at = @At("HEAD"), require = 0)
    private void spellclasses$nuclearBlast(HitResult hit, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        Level level = self.level();
        if (level.isClientSide) return;
        Vec3 at = hit.getLocation();
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(NUCLEAR_EXPLOSION);
        if (type.isPresent()) {
            try {
                Entity blast = type.get().create(level);
                if (blast != null) {
                    blast.setPos(at.x, at.y + BLAST_LIFT, at.z);
                    blast.getPersistentData().putBoolean(dev.qur1s.spellclasses.FireArrowBlast.CLEAN_TAG, true);
                    blast.getClass().getMethod("setSize", float.class).invoke(blast, NUCLEAR_SIZE);
                    // the crater is always dug, whatever the mobGriefing rule says
                    blast.getClass().getMethod("setNoGriefing", boolean.class).invoke(blast, false);
                    level.addFreshEntity(blast);
                    boom(level, at);
                    return;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // fall back to a TNT blast below
            }
        }
        level.explode(self, at.x, at.y, at.z, TNT_POWER, Level.ExplosionInteraction.TNT);
        boom(level, at);
    }

    /** The roar of the blast (heard from far away) and a long camera shake for everyone near it. */
    private static void boom(Level level, Vec3 at) {
        for (String name : new String[]{"explosion/nuclear_explosion", "explosion/main", "explosion/nuclear_explosion_rumble"}) {
            BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.fromNamespaceAndPath("createnuclear", name))
                    .ifPresent(sound -> level.playSound(null, at.x, at.y, at.z, sound, SoundSource.BLOCKS, 24.0f, 1.0f));
        }
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 24.0f, 0.5f);
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.fromNamespaceAndPath("createnuclear", "explosion/ringing")).ifPresent(ringing -> {
            for (net.minecraft.world.entity.player.Player player : level.players()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp && sp.position().distanceTo(at) < RINGING_RANGE) {
                    sp.playNotifySound(ringing, SoundSource.MASTER, 1.0f, 1.0f);
                }
            }
        });
        CameraShakeManager.addCameraShake(new CameraShakeData(level, SHAKE_TICKS, at, SHAKE_RADIUS));
    }
}
