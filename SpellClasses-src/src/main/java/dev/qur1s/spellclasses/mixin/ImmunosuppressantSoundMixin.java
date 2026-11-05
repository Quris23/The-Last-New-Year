package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Injecting an immunosuppressant played the Evoker's hurt sound (it sounds like a villager being hit): the volume is set to zero. */
@Pseudo
@Mixin(targets = "com.chromeware.humanity.ImmunosuppressantItem", remap = false)
public abstract class ImmunosuppressantSoundMixin {
    @ModifyArg(method = "inject(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            index = 6, remap = false, require = 0)
    private float spellclasses$noInjectionSound(float volume) {
        return 0.0F;
    }
}
