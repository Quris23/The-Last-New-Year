package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * The "Ножны" (scabbard) Curios slot: holds one weapon or staff, and a key swaps it with the main hand.
 */
public final class Scabbard {
    public static final String SLOT = "scabbard";

    private Scabbard() {
    }

    /** A weapon (anything that hits for damage, tools other than axes excluded) or any spell staff. */
    public static boolean accepts(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof StaffItem) return true;
        // Mining tools are not weapons - except Cataclysm's "forges", which are weapons built on PickaxeItem.
        boolean miningTool = stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
        if (miningTool && !isWeaponModItem(stack)) return false;
        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)
                    && entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
                    && entry.modifier().amount() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWeaponModItem(ItemStack stack) {
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return namespace.equals("cataclysm") || namespace.equals("cataclysm_spellbooks") || namespace.equals("irons_spellbooks");
    }

    /** Swaps the sheathed item with the main hand; a held item that does not fit leaves the sheath item to a free slot. */
    public static void swap(ServerPlayer player) {
        var inventory = CuriosApi.getCuriosInventory(player).orElse(null);
        if (inventory == null) return;
        var handler = inventory.getStacksHandler(SLOT).orElse(null);
        if (handler == null) return;
        IDynamicStackHandler stacks = handler.getStacks();
        if (stacks.getSlots() <= 0) return;

        ItemStack sheathed = stacks.getStackInSlot(0);
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (sheathed.isEmpty() && held.isEmpty()) return;

        if (held.isEmpty() || accepts(held)) {
            stacks.setStackInSlot(0, held.copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, sheathed.copy());
            return;
        }
        // The held item cannot be sheathed: put the sheathed one into a free inventory slot instead.
        int free = player.getInventory().getFreeSlot();
        if (free >= 0) {
            player.getInventory().setItem(free, sheathed.copy());
            stacks.setStackInSlot(0, ItemStack.EMPTY);
        }
    }
}
