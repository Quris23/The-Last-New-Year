package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.qur1s.spellclasses.implant.FaceImplants;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/** Draws the implanted Night Vision Goggles on the player's face with the goggles' own Curios renderer, only while they are on. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FaceLayers {
    private FaceLayers() {
    }

    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer player) {
                player.addLayer(new GogglesLayer(player));
                player.addLayer(new GauntletLayer(player, event.getContext().getItemInHandRenderer()));
            }
        }
    }

    private static final class GogglesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent;

        GogglesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
            this.parent = parent;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing,
                           float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (player.isInvisible() || player.isSpectator()) return;
            ItemStack goggles = FaceImplants.find(player, FaceImplants::isGoggles);
            if (goggles.isEmpty() || !FaceImplants.on(goggles)) return;
            CuriosRendererRegistry.getRenderer(goggles.getItem()).ifPresent(renderer ->
                    renderer.render(goggles, new SlotContext("head", player, 0, false, true), pose, this.parent, buffers, light,
                            limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch));
        }
    }

    /** Draws the implanted gauntlets on the arms while the arm implants are deployed (combat mode), like an item held in that hand. */
    private static final class GauntletLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final net.minecraft.client.renderer.ItemInHandRenderer itemRenderer;

        GauntletLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                      net.minecraft.client.renderer.ItemInHandRenderer itemRenderer) {
            super(parent);
            this.itemRenderer = itemRenderer;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing,
                           float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (player.isInvisible() || player.isSpectator()) return;
            for (net.minecraft.world.entity.HumanoidArm side : net.minecraft.world.entity.HumanoidArm.values()) {
                if (!dev.qur1s.spellclasses.implant.GauntletImplants.deployedOn(player, side)) continue;
                ItemStack gauntlet = com.chromeware.cyberware.ArmImplants.get(player, side).item();
                boolean left = side == net.minecraft.world.entity.HumanoidArm.LEFT;
                pose.pushPose();
                this.getParentModel().translateToHand(side, pose);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
                pose.translate((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
                this.itemRenderer.renderItem(player, gauntlet, left
                        ? net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                        : net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, left, pose, buffers, light);
                pose.popPose();
            }
        }
    }
}
