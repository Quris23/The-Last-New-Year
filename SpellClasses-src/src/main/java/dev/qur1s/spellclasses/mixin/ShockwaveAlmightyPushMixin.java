package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.WindPushOnShockwave;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.lightning.ShockwaveSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shockwave also releases Almighty Push (Wind's Spellbooks), at 0.75 of Shockwave's radius. */
@Mixin(value = ShockwaveSpell.class, remap = false)
public abstract class ShockwaveAlmightyPushMixin {
    @Inject(method = "onCast", at = @At("TAIL"), require = 0)
    private void spellclasses$almightyPush(Level level, int spellLevel, LivingEntity caster, CastSource source,
                                           MagicData magicData, CallbackInfo ci) {
        WindPushOnShockwave.spawn(level, spellLevel, caster,
                ((ShockwaveSpell) (Object) this).getRadius(spellLevel, caster), 0.75f);
    }
}
