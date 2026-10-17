package dev.qur1s.legspyglass.mixin;

import dev.qur1s.legspyglass.client.ClientScope;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Raises the main arm into the vanilla spyglass pose while the player looks through the leg spyglass. */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "setModelProperties", at = @At("TAIL"), remap = false, require = 0)
    private void legspyglass$spyglassPose(AbstractClientPlayer player, CallbackInfo ci) {
        if (!ClientScope.isScoping(player)) return;
        PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
        if (player.getMainArm() == HumanoidArm.LEFT) model.leftArmPose = HumanoidModel.ArmPose.SPYGLASS;
        else model.rightArmPose = HumanoidModel.ArmPose.SPYGLASS;
    }
}
