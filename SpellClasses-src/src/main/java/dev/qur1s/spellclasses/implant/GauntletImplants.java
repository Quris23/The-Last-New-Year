package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.ArmImplants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cataclysm's Gauntlets of Guard, Bulwark and Maelstrom as arm implants (installed in the left / right arm sockets, implant
 * capacity 25). They work only while the arm implants are deployed (Chromeware's combat mode); the attack button (main arm) and the
 * use button (other arm) hold the ability of the gauntlet on that arm, exactly like the Gorilla Arms: the gauntlet is "used" as if
 * it were held (its use and release are run directly).
 */
public final class GauntletImplants {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "spellclasses");
    /** Combat mode of the gauntlets (synced to the client, which asks it when the buttons are pressed). */
    public static final Supplier<AttachmentType<Boolean>> DEPLOYED = ATTACHMENTS.register("gauntlets_deployed",
            () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());

    /** What is being used on which arm of a player right now. */
    private static final Map<UUID, Map<HumanoidArm, Hold>> USING = new ConcurrentHashMap<>();

    private GauntletImplants() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    public static boolean isGauntlet(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("cataclysm") && switch (id.getPath()) {
            case "gauntlet_of_guard", "gauntlet_of_bulwark", "gauntlet_of_maelstrom" -> true;
            default -> false;
        };
    }

    public static boolean has(Player player) {
        return ArmImplants.has(player, GauntletImplants::isGauntlet);
    }

    public static boolean deployed(Player player) {
        return player.getData(DEPLOYED);
    }

    public static boolean deployedOn(Player player, HumanoidArm side) {
        return deployed(player) && ArmImplants.has(player, side, GauntletImplants::isGauntlet);
    }

    public static void setDeployed(Player player, boolean value) {
        if (player.getData(DEPLOYED) != value) player.setData(DEPLOYED, value);
    }

    private static final Map<UUID, Long> DASH_UNTIL = new ConcurrentHashMap<>();

    private record Hold(ItemStack stack, long start) {
    }

    private static boolean using(Player player, HumanoidArm side) {
        Map<HumanoidArm, Hold> map = USING.get(player.getUUID());
        return map != null && map.containsKey(side);
    }

    /**
     * Start of the ability (button held). The abilities of Cataclysm's gauntlets depend only on the item and on the ticks it has been
     * used for, so the gauntlet is run directly: held ticks go to onUseTick, the release to releaseUsing, as the game does it for a
     * held item.
     */
    public static void charge(Player player, HumanoidArm side) {
        if (!deployedOn(player, side) || using(player, side)) return;
        ItemStack gauntlet = ArmImplants.get(player, side).item();
        if (player.getCooldowns().isOnCooldown(gauntlet.getItem())) {
            player.displayClientMessage(Component.literal("Перчатка перезаряжается: способность не готова").withStyle(ChatFormatting.RED), true);
            return;
        }
        USING.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(HumanoidArm.class))
                .put(side, new Hold(gauntlet, player.level().getGameTime()));
    }

    private static int remaining(Player player, Hold hold) {
        return hold.stack().getUseDuration(player) - (int) (player.level().getGameTime() - hold.start());
    }

    /** The button is let go: the ability is released (Maelstrom casts its vortex, Bulwark dashes after a full charge). */
    public static void release(Player player, HumanoidArm side) {
        Map<HumanoidArm, Hold> map = USING.get(player.getUUID());
        Hold hold = map == null ? null : map.remove(side);
        if (hold == null) return;
        int left = Math.max(0, remaining(player, hold));
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        // the Bulwark's dash goes where the player looks; it is turned at the creature the player is looking towards
        var target = BuiltInRegistries.ITEM.getKey(hold.stack().getItem()).getPath().equals("gauntlet_of_bulwark") ? dashTarget(player) : null;
        if (target != null) {
            net.minecraft.world.phys.Vec3 to = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
            player.setYRot((float) (net.minecraft.util.Mth.atan2(-to.x, to.z) * 57.29577951308232));
            player.setXRot((float) (-net.minecraft.util.Mth.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)) * 57.29577951308232));
        }
        try {
            hold.stack().getItem().releaseUsing(hold.stack(), player.level(), player, left);
        } finally {
            player.setYRot(yaw);
            player.setXRot(pitch);
        }
        // the player's own movement is the client's: the push the gauntlet gave is sent to it (the dash lasts several ticks)
        player.hurtMarked = true;
        DASH_UNTIL.put(player.getUUID(), player.level().getGameTime() + 14);
    }

    /** The living creature closest to the line of sight within 16 blocks (at most about 45 degrees off it), or null. */
    private static net.minecraft.world.entity.LivingEntity dashTarget(Player player) {
        net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
        net.minecraft.world.entity.LivingEntity best = null;
        double bestAngle = Math.toRadians(45.0);
        for (var entity : player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(16.0), e -> e != player && e.isAlive() && !e.isSpectator())) {
            net.minecraft.world.phys.Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance < 1.0E-3) continue;
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, look.dot(to) / distance)));
            if (angle < bestAngle) {
                bestAngle = angle;
                best = entity;
            }
        }
        return best;
    }

    /** A short click: used and released at once. */
    public static void act(Player player, HumanoidArm side) {
        charge(player, side);
        release(player, side);
    }

    public static void cancel(Player player, HumanoidArm side) {
        Map<HumanoidArm, Hold> map = USING.get(player.getUUID());
        if (map != null) map.remove(side);
    }

    /** Arm implants retracted (or removed): combat mode off, every use stopped. */
    public static void retract(Player player) {
        if (player.level().isClientSide()) return;
        USING.remove(player.getUUID());
        setDeployed(player, false);
    }

    /** Every tick on the server: forgets uses that ended by themselves and switches the mode off when no gauntlet is left. */
    public static void tick(Player player) {
        Map<HumanoidArm, Hold> map = USING.get(player.getUUID());
        if (map != null) {
            map.keySet().removeIf(side -> !deployedOn(player, side));
            for (Hold hold : map.values()) {
                int left = remaining(player, hold);
                if (left > 0) hold.stack().getItem().onUseTick(player.level(), player, hold.stack(), left);
            }
            if (map.isEmpty()) USING.remove(player.getUUID());
        }
        Long until = DASH_UNTIL.get(player.getUUID());
        if (until != null) {
            if (player.level().getGameTime() <= until) player.hurtMarked = true;
            else DASH_UNTIL.remove(player.getUUID());
        }
        if (deployed(player) && !has(player)) setDeployed(player, false);
    }

    public static void forget(UUID player) {
        USING.remove(player);
        DASH_UNTIL.remove(player);
    }
}
