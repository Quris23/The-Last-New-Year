package dev.qur1s.spellclasses.client;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import uk.co.iceconchy.aerowarptics.registry.AWBlocks;

/**
 * Makes the Rift Gate frame and the Rift Gate controller join up the way Create's casings do: Create's own
 * connected-texture system with the 8x8 "omnidirectional" sheet {@code rift_gate_frame_connected} (the same layout as
 * Create's {@code *_connected.png}). Frame blocks connect to frames and to the controller and vice versa, so a whole
 * ring reads as one gold-trimmed surface, shaft-end faces included.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RiftGateConnectedTextures {
    private RiftGateConnectedTextures() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded("aerowarptics") || !ModList.get().isLoaded("create")) {
            return;
        }
        event.enqueueWork(() -> {
            ResourceLocation sheet = id("block/rift_gate_frame_connected");
            CTSpriteShiftEntry frame = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, id("block/rift_gate_frame"), sheet);
            CTSpriteShiftEntry side = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, id("block/rift_gate_side"), sheet);
            var models = CreateClient.MODEL_SWAPPER.getCustomBlockModels();
            models.register(id("rift_gate_frame"), model -> new CTModel(model, new GateParts(frame)));
            CTSpriteShiftEntry end = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, id("block/rift_gate_end"),
                    id("block/rift_gate_end_connected"));
            models.register(id("rift_gate"), model -> new CTModel(model, new ControllerParts(side, end)));
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("aerowarptics", path);
    }

    private static boolean isGatePart(BlockState state) {
        return state.is(AWBlocks.RIFT_GATE_FRAME.get()) || state.is(AWBlocks.RIFT_GATE.get());
    }

    /**
     * The controller: side faces and shaft-end faces each get their own sheet ({@code rift_gate_end_connected} is the
     * frame sheet with the shaft ring drawn in the middle of every tile, so the ring stays and the gold trim only
     * remains on the edges that touch no other gate part). The blockstate uses {@code uvlock}, so textures stay
     * aligned with the world however the controller is turned and the plain cube rules apply.
     */
    private static final class ControllerParts extends SimpleCTBehaviour {
        private final CTSpriteShiftEntry endShift;

        ControllerParts(CTSpriteShiftEntry sideShift, CTSpriteShiftEntry endShift) {
            super(sideShift);
            this.endShift = endShift;
        }

        @Override
        public CTSpriteShiftEntry getShift(BlockState state, Direction face, TextureAtlasSprite sprite) {
            return face.getAxis() == state.getValue(DirectionalKineticBlock.FACING).getAxis() ? endShift : shift;
        }

        @Override
        public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
                                  BlockPos otherPos, Direction face) {
            return isGatePart(other) && !isBeingBlocked(state, reader, pos, otherPos, face);
        }
    }

    /** A frame block connects to any other gate part: frame or controller. */
    private static final class GateParts extends SimpleCTBehaviour {
        GateParts(CTSpriteShiftEntry shift) {
            super(shift);
        }

        @Override
        public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
                                  BlockPos otherPos, Direction face) {
            return isGatePart(other) && !isBeingBlocked(state, reader, pos, otherPos, face);
        }
    }
}
