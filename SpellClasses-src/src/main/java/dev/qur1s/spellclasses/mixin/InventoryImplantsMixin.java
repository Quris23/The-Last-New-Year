package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.OctoRigBackSlot;
import dev.qur1s.spellclasses.implant.ImplantUpgrades;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chromeware installs a few non-catalogue items (Blood Pump, Dense Marrow...) straight from the hand into the sockets of
 * a category. Octo Arms tentacle rigs are installed the same way into the Skeleton category (index 2).
 */
@Pseudo
@Mixin(targets = "com.chromeware.cyberware.InventoryImplants", remap = false)
public abstract class InventoryImplantsMixin {
    @Inject(method = "supported(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true,
            remap = false, require = 0)
    private static void spellclasses$tentacleRigSupported(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && (OctoRigBackSlot.isTentacleRig(stack) || ImplantUpgrades.installable(stack))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "category(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true,
            remap = false, require = 0)
    private static void spellclasses$tentacleRigCategory(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (OctoRigBackSlot.isTentacleRig(stack)) {
            cir.setReturnValue(2);
        } else if (ImplantUpgrades.installable(stack)) {
            cir.setReturnValue(ImplantUpgrades.rule(stack).category());
        }
    }

    @org.spongepowered.asm.mixin.Shadow(remap = false)
    private static java.util.Map<net.minecraft.world.entity.player.Player, Integer> REQUESTED;

    /**
     * Chromeware puts a held implant into the socket that already holds the same item (it allows one of each). Backpack upgrades
     * may be installed several times (two Tanks for two Pumps): they go into the requested socket, else the first free one.
     */
    @Inject(method = "socket(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$freeSocket(net.minecraft.world.entity.player.Player player, ItemStack held,
                                                CallbackInfoReturnable<Integer> cir) {
        // only one Tentacle Rig may be worn: no free socket for a second one
        if (OctoRigBackSlot.isTentacleRig(held) && !OctoRigBackSlot.installedRig(player).isEmpty()) {
            cir.setReturnValue(-1);
            return;
        }
        boolean k = dev.qur1s.spellclasses.implant.KSlots.isK(held);
        if (!k && !ImplantUpgrades.forBackpack(held)) return;
        int category = ImplantUpgrades.rule(held).category();
        var slots = com.chromeware.cyberware.InventoryImplants.get(player, category);
        int count = com.chromeware.cyberware.ImplantCategory.byIndex(category).sockets;
        int first = k ? dev.qur1s.spellclasses.implant.KSlots.FIRST : 0;
        int last = k ? dev.qur1s.spellclasses.implant.KSlots.LAST : count - 1;
        Integer requested = REQUESTED.get(player);
        if (requested != null && requested >= first && requested <= last && requested < count) {
            cir.setReturnValue(requested);
            return;
        }
        for (int i = first; i <= last && i < count; i++) {
            if (!slots.get(i).installed()) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    /** The К sockets are category 8: its sockets are stored in our own attachment. */
    @Inject(method = "get(Lnet/minecraft/world/entity/player/Player;I)Lcom/chromeware/cyberware/CyberwareSlots;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$kGet(net.minecraft.world.entity.player.Player player, int category,
                                          CallbackInfoReturnable<com.chromeware.cyberware.CyberwareSlots> cir) {
        if (category == dev.qur1s.spellclasses.implant.KSlots.CATEGORY) {
            cir.setReturnValue(player.getData(dev.qur1s.spellclasses.implant.KSlots.SLOTS));
        }
    }

    @Inject(method = "set(Lnet/minecraft/world/entity/player/Player;ILcom/chromeware/cyberware/CyberwareSlots;)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$kSet(net.minecraft.world.entity.player.Player player, int category,
                                          com.chromeware.cyberware.CyberwareSlots slots,
                                          org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (category == dev.qur1s.spellclasses.implant.KSlots.CATEGORY) {
            player.setData(dev.qur1s.spellclasses.implant.KSlots.SLOTS, slots);
            ci.cancel();
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/player/Player;II)Z",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$kRemove(net.minecraft.world.entity.player.Player player, int category, int index,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (category == dev.qur1s.spellclasses.implant.KSlots.CATEGORY) {
            cir.setReturnValue(dev.qur1s.spellclasses.implant.KSlots.remove(player, index));
        }
    }

    /** Implant capacity: the items in the К sockets count too. */
    @Pseudo
    @Mixin(targets = "com.chromeware.cyberware.CyberwareCapacity", remap = false)
    public abstract static class Capacity {
        @Inject(method = "installed(Lnet/minecraft/world/entity/player/Player;)Ljava/util/List;",
                at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$kItems(net.minecraft.world.entity.player.Player player,
                                                CallbackInfoReturnable<java.util.List<ItemStack>> cir) {
            var extra = dev.qur1s.spellclasses.implant.KSlots.items(player);
            if (extra.isEmpty()) return;
            var all = new java.util.ArrayList<>(cir.getReturnValue());
            all.addAll(extra);
            cir.setReturnValue(java.util.List.copyOf(all));
        }
    }

    /** The surgery chair refuses "already installed" for the same item: not for backpack upgrades. */
    @Pseudo
    @Mixin(targets = "com.chromeware.surgery.Surgery", remap = false)
    public abstract static class Chair {
        @Inject(method = "duplicate(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;)Z",
                at = @At("HEAD"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$upgradesMayRepeat(net.minecraft.world.entity.player.Player player, ItemStack stack,
                                                           net.minecraft.world.entity.HumanoidArm arm, CallbackInfoReturnable<Boolean> cir) {
            if (ImplantUpgrades.forBackpack(stack)) cir.setReturnValue(false);
            // a second Tentacle Rig (copper or netherite) counts as already installed: only one can be worn
            if (OctoRigBackSlot.isTentacleRig(stack) && !OctoRigBackSlot.installedRig(player).isEmpty()) cir.setReturnValue(true);
        }

        /** The gauntlets are arm hardware: the arm sockets' stock offers them and the chair puts them there. */
        @Inject(method = "armHardware(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$gauntletsAreArmHardware(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValueZ() && dev.qur1s.spellclasses.implant.GauntletImplants.isGauntlet(stack)) cir.setReturnValue(true);
        }

        /** An Altimeter and a Compass belong to the Operating System group (its К sockets). */
        @Inject(method = "belongs(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Z",
                at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$kBelongs(ItemStack item, String category, CallbackInfoReturnable<Boolean> cir) {
            if ("operating_system".equals(category) && dev.qur1s.spellclasses.implant.KSlots.isK(item)) cir.setReturnValue(true);
        }

        @Inject(method = "installed(Lnet/minecraft/world/entity/player/Player;Lcom/chromeware/surgery/Surgery$Removal;)Lnet/minecraft/world/item/ItemStack;",
                at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$kInstalled(net.minecraft.world.entity.player.Player player,
                                                    com.chromeware.surgery.Surgery.Removal removal,
                                                    CallbackInfoReturnable<ItemStack> cir) {
            if (removal.category() == dev.qur1s.spellclasses.implant.KSlots.CATEGORY
                    && removal.index() >= dev.qur1s.spellclasses.implant.KSlots.FIRST
                    && removal.index() <= dev.qur1s.spellclasses.implant.KSlots.LAST) {
                cir.setReturnValue(dev.qur1s.spellclasses.implant.KSlots.slot(player, removal.index()));
            }
        }

        /** The chair's chosen socket is passed on as the requested one, so an implant lands in the К socket that was picked. */
        @Inject(method = "aim(Lnet/minecraft/world/entity/player/Player;Lcom/chromeware/surgery/Surgery$Request;)V",
                at = @At("RETURN"), remap = false, require = 0)
        private static void spellclasses$kAim(net.minecraft.world.entity.player.Player player,
                                              com.chromeware.surgery.Surgery.Request request,
                                              org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
            if ("operating_system".equals(request.category()) && request.index() >= dev.qur1s.spellclasses.implant.KSlots.FIRST
                    && dev.qur1s.spellclasses.implant.KSlots.isK(request.item())) {
                com.chromeware.cyberware.InventoryImplants.request(player, request.index());
            }
        }
    }

    /** CyberwareStats: what a tentacle rig costs in implant capacity (and so in permanent humanity loss). */
    @Pseudo
    @Mixin(targets = "com.chromeware.cyberware.CyberwareStats", remap = false)
    public abstract static class Stats {
        private static java.lang.reflect.Constructor<?> spellclasses$ctor;
        private static boolean spellclasses$broken;

        @Inject(method = "get(Lnet/minecraft/world/item/ItemStack;)Lcom/chromeware/cyberware/CyberwareStats$Stats;",
                at = @At("HEAD"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$rigCapacity(ItemStack stack, CallbackInfoReturnable<Object> cir) {
            if (dev.qur1s.spellclasses.implant.GauntletImplants.isGauntlet(stack) && !spellclasses$broken) {
                try {
                    if (spellclasses$ctor == null) {
                        spellclasses$ctor = Class.forName("com.chromeware.cyberware.CyberwareStats$Stats").getConstructor(int.class, int.class);
                    }
                    cir.setReturnValue(spellclasses$ctor.newInstance(25, 0));
                } catch (Throwable t) {
                    spellclasses$broken = true;
                }
                return;
            }
            boolean rig = OctoRigBackSlot.isTentacleRig(stack);
            if (spellclasses$broken || (!rig && !ImplantUpgrades.installable(stack))) return;
            try {
                if (spellclasses$ctor == null) {
                    spellclasses$ctor = Class.forName("com.chromeware.cyberware.CyberwareStats$Stats")
                            .getConstructor(int.class, int.class);
                }
                cir.setReturnValue(spellclasses$ctor.newInstance(rig ? OctoRigBackSlot.rigCapacity(stack) : ImplantUpgrades.rule(stack).capacity(), 0));
            } catch (Throwable t) {
                spellclasses$broken = true;
            }
        }
    }
}
