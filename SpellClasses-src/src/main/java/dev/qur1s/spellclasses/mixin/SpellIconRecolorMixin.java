package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.client.SpellIconRecolor;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Spell icons follow the school colour chosen in Recolor Tablet (client side only, listed in the "client" mixins). */
@Mixin(value = AbstractSpell.class, remap = false)
public abstract class SpellIconRecolorMixin {
    @Inject(method = "getSpellIconResource", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$recolorIcon(CallbackInfoReturnable<ResourceLocation> cir) {
        if (!ModList.get().isLoaded("recolor_tablet")) return;
        cir.setReturnValue(SpellIconRecolor.recolor((AbstractSpell) (Object) this, cir.getReturnValue()));
    }
}
