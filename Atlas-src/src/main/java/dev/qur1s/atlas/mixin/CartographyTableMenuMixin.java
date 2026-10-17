package dev.qur1s.atlas.mixin;

import dev.qur1s.atlas.AtlasComponents;
import dev.qur1s.atlas.AtlasCopyResultSlot;
import dev.qur1s.atlas.AtlasItems;
import dev.qur1s.atlas.AtlasMapSlot;
import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/** Lets the cartography table copy an Atlas: Atlas in the map slot, an empty map in the other, the copy comes out. */
@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin extends AbstractContainerMenu {
    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final public Container container;
    @Shadow @Final private ResultContainer resultContainer;

    protected CartographyTableMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("RETURN"))
    private void atlas$replaceSlots(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        Slot map = new AtlasMapSlot(this.container, 0, 15, 15);
        map.index = 0;
        this.slots.set(0, map);
        Slot extra = new dev.qur1s.atlas.AtlasExtraSlot(this.container, 1, 15, 52);
        extra.index = 1;
        this.slots.set(1, extra);
        Slot result = new AtlasCopyResultSlot(this.container, this.resultContainer, 2, 145, 39, access);
        result.index = 2;
        this.slots.set(2, result);
    }

    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true)
    private void atlas$copyAtlas(ItemStack map, ItemStack additional, ItemStack result, CallbackInfo ci) {
        if (!map.is(AtlasItems.ATLAS.get())) return;
        ci.cancel();
        this.access.execute((level, pos) -> {
            ItemStack copy = ItemStack.EMPTY;
            if (additional.is(Items.MAP)) {
                copy = map.copyWithCount(1);
            } else if (additional.is(AtlasItems.ATLAS.get()) && level.getServer() != null) {
                // a second Atlas is the blank for the copy, so it has to be empty
                UUID other = additional.get(AtlasComponents.ATLAS_ID.get());
                if (other == null || AtlasData.get(level.getServer()).isEmpty(other)) copy = map.copyWithCount(1);
            }
            if (!copy.isEmpty()) copy.set(AtlasComponents.ATLAS_ID.get(), UUID.randomUUID());
            if (!ItemStack.matches(copy, this.resultContainer.getItem(2))) {
                this.resultContainer.setItem(2, copy);
                this.broadcastChanges();
            }
        });
    }

    /** The Atlas alone in the table: the result is the map that was poured in last, which can be taken out again. */
    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void atlas$extractLast(Container changed, CallbackInfo ci) {
        ItemStack map = this.container.getItem(0);
        if (!map.is(AtlasItems.ATLAS.get()) || !this.container.getItem(1).isEmpty()) return;
        ci.cancel();
        this.access.execute((level, pos) -> {
            ItemStack preview = ItemStack.EMPTY;
            UUID id = map.get(AtlasComponents.ATLAS_ID.get());
            if (id != null && level instanceof net.minecraft.server.level.ServerLevel server && server.getServer() != null) {
                preview = dev.qur1s.atlas.AtlasExtract.preview(server, id, pos);
            }
            if (!ItemStack.matches(preview, this.resultContainer.getItem(2))) {
                this.resultContainer.setItem(2, preview);
            }
        });
        this.broadcastChanges();
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void atlas$shiftClickAtlas(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 3) return;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.getItem().is(AtlasItems.ATLAS.get())) return;
        ItemStack stack = slot.getItem();
        ItemStack before = stack.copy();
        // the first Atlas goes to the map slot, a second one to the other slot
        boolean first = !this.slots.get(0).hasItem();
        if (!this.moveItemStackTo(stack, first ? 0 : 1, first ? 1 : 2, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        cir.setReturnValue(before);
    }
}
