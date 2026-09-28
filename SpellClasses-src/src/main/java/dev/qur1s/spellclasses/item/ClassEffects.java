package dev.qur1s.spellclasses.item;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClassEffects {
    private ClassEffects() {
    }

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT, "spellclasses");

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MobEffect, MobEffect> LAPLACE_FACTOR = EFFECTS.register("laplace_factor",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x39E6C9) {}
                    .addAttributeModifier((Holder<Attribute>) (Holder<?>) AttributeRegistry.MAX_MANA,
                            ResourceLocation.fromNamespaceAndPath("spellclasses", "laplace_factor_mana"),
                            2000.0, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier((Holder<Attribute>) (Holder<?>) AttributeRegistry.COOLDOWN_REDUCTION,
                            ResourceLocation.fromNamespaceAndPath("spellclasses", "laplace_factor_cooldown"),
                            0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
