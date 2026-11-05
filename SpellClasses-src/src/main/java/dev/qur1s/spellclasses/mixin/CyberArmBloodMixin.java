package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.qur1s.spellclasses.client.ArmBlood;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Hemodynamics only paints Chromeware's arm hardware (blades, Gorilla Arms...): the bare cyber arm is drawn by
 * CyberLimbRenderer and stayed clean outside the blade mode. The whole arm draw now runs inside the same Hemodynamics
 * wrapper that the hardware renderers use.
 */
@Pseudo
@Mixin(targets = "com.chromeware.client.body.CyberLimbRenderer", remap = false)
public abstract class CyberArmBloodMixin {
    @WrapMethod(method = "renderArm(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/entity/HumanoidArm;"
            + "Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V", remap = false, require = 0)
    private static void spellclasses$bloodOnCyberArm(AbstractClientPlayer player, HumanoidArm arm, ModelPart part,
                                                     PoseStack stack, MultiBufferSource buffers, int light,
                                                     Operation<Void> original) {
        ArmBlood.around(player, arm, () -> original.call(player, arm, part, stack, buffers, light));
    }
}
