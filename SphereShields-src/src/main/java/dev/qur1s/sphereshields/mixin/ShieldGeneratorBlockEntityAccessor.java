package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
public interface ShieldGeneratorBlockEntityAccessor {
    @Accessor("active")
    boolean sphereshields$isActive();

    @Accessor("groundShieldMode")
    boolean sphereshields$isGroundShieldMode();

    @Accessor("groundShieldRadius")
    double sphereshields$getGroundShieldRadius();

    @Accessor("shieldHeat")
    int sphereshields$getShieldHeat();

    @Accessor("fuelTier")
    int sphereshields$getFuelTier();
}
