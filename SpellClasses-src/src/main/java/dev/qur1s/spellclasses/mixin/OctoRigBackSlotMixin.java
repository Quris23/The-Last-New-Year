package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qur1s.spellclasses.OctoRigBackSlot;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Octo Arms: the Tentacle Rig is worn in the Curios "back" slot, not the chest slot. Every call that asks the
 * wearer's chest stack is answered with the back-slot rig (see {@link OctoRigBackSlot}); the item also refuses to be
 * put on as armor. One mixin class per kind of receiver, because the calls are made through different types.
 */
public final class OctoRigBackSlotMixin {
    private OctoRigBackSlotMixin() {
    }

    private static final String GET = "getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;";

    @Mixin(targets = {"dev.tentaworks.rig.TentacleRigItem", "dev.tentaworks.rig.RigAir"}, remap = false)
    public abstract static class OnPlayer {
        @WrapOperation(method = {"wornTier", "reach(Lnet/minecraft/world/entity/player/Player;)D", "worn"}, remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;" + GET))
        private static ItemStack spellclasses$backSlot(Player player, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(player, slot, original.call(player, slot));
        }
    }

    @Mixin(targets = "dev.tentaworks.network.RigSettingsPacket", remap = false)
    public abstract static class OnPacket {
        @WrapOperation(method = "handle", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;" + GET))
        private static ItemStack spellclasses$backSlot(ServerPlayer player, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(player, slot, original.call(player, slot));
        }
    }

    @Mixin(targets = "dev.tentaworks.client.RigSettingsScreen", remap = false)
    public abstract static class OnScreen {
        @WrapOperation(method = "open", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;" + GET))
        private static ItemStack spellclasses$backSlot(LocalPlayer player, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(player, slot, original.call(player, slot));
        }
    }

    @Mixin(targets = "dev.tentaworks.client.TentacleLayer", remap = false)
    public abstract static class OnLayer {
        @WrapOperation(method = "render", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;" + GET))
        private ItemStack spellclasses$backSlotWorld(AbstractClientPlayer player, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(player, slot, original.call(player, slot));
        }

        @WrapOperation(method = "onFirstPerson", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;" + GET))
        private static ItemStack spellclasses$backSlotFirstPerson(LocalPlayer player, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(player, slot, original.call(player, slot));
        }
    }

    /** Create's backpack model is drawn by its own layer from the chest slot; it should draw the back-slot one too. */
    @Mixin(targets = "com.simibubi.create.content.equipment.armor.BacktankArmorLayer", remap = false)
    public abstract static class OnCreateLayer {
        @WrapOperation(method = "render", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;" + GET))
        private ItemStack spellclasses$backSlot(LivingEntity entity, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(entity, slot, original.call(entity, slot));
        }
    }

    /** Backtanks (Create's and the rigs, which extend them) are worn in the back slot and never as a chestplate. */
    @Mixin(targets = "com.simibubi.create.content.equipment.armor.BacktankItem", remap = false)
    public abstract static class NoChestEquip {
        @WrapOperation(method = "getWornBy", remap = false, require = 0,
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;" + GET))
        private static ItemStack spellclasses$backSlot(LivingEntity entity, EquipmentSlot slot, Operation<ItemStack> original) {
            return OctoRigBackSlot.stackFor(entity, slot, original.call(entity, slot));
        }

        /** Right-click from the hand would put it on as a chestplate: send it to the back slot instead. */
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (OctoRigBackSlot.isTentacleRig(stack)) return InteractionResultHolder.pass(stack);
            if (!level.isClientSide) OctoRigBackSlot.equipToBack(player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
            return false;
        }
    }
}
