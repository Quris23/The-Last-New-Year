package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.ClassSchools;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Wind school (Wind's Spellbooks) is folded into Storm (Iron's Lightning): its spells show the Storm
 * name and colour, scale with Storm spell power, are resisted by Storm resistance and are focused by Storm
 * staves/books. Only the id stays Wind's own, so the spells keep registering under their original school.
 */
@Mixin(value = SchoolType.class, remap = false)
public abstract class WindSchoolIsStormMixin {
    @org.spongepowered.asm.mixin.Shadow
    public abstract ResourceLocation getId();

    private SchoolType spellclasses$storm() {
        return ClassSchools.stormFor(getId());
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$name(CallbackInfoReturnable<Component> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getDisplayName());
    }

    @Inject(method = "getPowerFor", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$power(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getPowerFor(entity));
    }

    @Inject(method = "getResistanceFor", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$resist(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getResistanceFor(entity));
    }

    @Inject(method = "getCastSound", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$sound(CallbackInfoReturnable<SoundEvent> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getCastSound());
    }

    @Inject(method = "getDamageType", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$damage(CallbackInfoReturnable<ResourceKey<DamageType>> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getDamageType());
    }

    @Inject(method = "getFocus", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$focus(CallbackInfoReturnable<TagKey<Item>> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getFocus());
    }

    @Inject(method = "isFocus", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$isFocus(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.isFocus(stack));
    }

    @Inject(method = "getTargetingColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void spellclasses$colour(CallbackInfoReturnable<Vector3f> cir) {
        SchoolType s = spellclasses$storm();
        if (s != null) cir.setReturnValue(s.getTargetingColor());
    }
}
