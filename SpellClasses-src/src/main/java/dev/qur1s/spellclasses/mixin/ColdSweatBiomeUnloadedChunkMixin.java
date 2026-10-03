package dev.qur1s.spellclasses.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.BiomeTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Cold Sweat samples the biome around an entity on the server tick. When a chunk it asks for is not loaded
 * (a brand-new world whose spawn chunk is still being generated, the edge of the loaded area), the vanilla
 * lookup blocks the server thread on a chunk future that the same thread has to complete - the world then
 * hangs at 100% "loading" forever. Treat unloaded chunks as "no data": the entity keeps its temperature for
 * that tick and is measured again on the next one.
 */
@Mixin(value = BiomeTempModifier.class, remap = false)
public abstract class ColdSweatBiomeUnloadedChunkMixin {
    private static final ThreadLocal<Level> SPELLCLASSES$LEVEL = new ThreadLocal<>();

    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private void spellclasses$skipWhenUnloaded(LivingEntity entity, Temperature.Trait trait,
                                               CallbackInfoReturnable<Function<Double, Double>> cir) {
        BlockPos pos = entity.blockPosition();
        if (!entity.level().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            cir.setReturnValue(temp -> temp);
            return;
        }
        SPELLCLASSES$LEVEL.set(entity.level());
    }

    @Inject(method = "calculate", at = @At("RETURN"))
    private void spellclasses$done(LivingEntity entity, Temperature.Trait trait,
                                   CallbackInfoReturnable<Function<Double, Double>> cir) {
        SPELLCLASSES$LEVEL.remove();
    }

    @Redirect(method = "calculate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/biome/BiomeManager;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> spellclasses$biomeIfLoaded(BiomeManager manager, BlockPos pos) {
        Level level = SPELLCLASSES$LEVEL.get();
        if (level == null || level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return manager.getBiome(pos);
        }
        // A keyless holder is skipped by Cold Sweat's own "holder.unwrapKey().isEmpty()" check.
        return Holder.direct(level.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
    }
}
