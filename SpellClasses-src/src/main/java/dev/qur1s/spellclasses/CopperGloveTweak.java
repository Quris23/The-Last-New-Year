package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * Somake's Copper Glove is a hands-slot curio: worn, it gives +15% Lightning (Storm) spell power without being held
 * (the held Fire buff was removed, see SomakeWeaponSpellPowerMixin). The slot tag is in data/curios/tags/item/hands.json.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class CopperGloveTweak {
    private CopperGloveTweak() {
    }

    private static final ResourceLocation COPPER_GLOVE = ResourceLocation.fromNamespaceAndPath("somakespells", "copper_glove");
    /** Cataclysm's three gauntlets and the Ice and Sandworm gauntlets (Block Factory's Bosses) work the same way: hands-slot curios that are used (right click) and hit from the slot. */
    private static final java.util.Set<ResourceLocation> GAUNTLETS = java.util.Set.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_guard"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_bulwark"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_maelstrom"),
            ResourceLocation.fromNamespaceAndPath("block_factorys_bosses", "ice_gauntlet"),
            ResourceLocation.fromNamespaceAndPath("block_factorys_bosses", "sandworm_gauntlet"),
            ResourceLocation.fromNamespaceAndPath("legendary_monsters", "fiery_jaw"));

    public static boolean isGauntlet(net.minecraft.world.item.ItemStack stack) {
        return !stack.isEmpty() && GAUNTLETS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isHandWeapon(net.minecraft.world.item.ItemStack stack) {
        return !stack.isEmpty() && (isGauntlet(stack) || COPPER_GLOVE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem())));
    }

    private static final double STORM_POWER = 0.15;
    private static final double ATTACK_DAMAGE = 11.0;
    private static final double ATTACK_SPEED = -2.6;

    /**
     * With two gloves worn the hits must not add up (two -2.6 swing speeds make attacking impossible), so the hit
     * modifiers (damage, swing speed) come only from the lowest-numbered hands slot that holds a glove.
     */
    private static boolean firstHandWeaponSlot(CurioAttributeModifierEvent event) {
        var inventory = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(event.getSlotContext().entity()).orElse(null);
        if (inventory == null) return true;
        var handler = inventory.getStacksHandler(event.getSlotContext().identifier()).orElse(null);
        if (handler == null) return true;
        var stacks = handler.getStacks();
        for (int i = 0; i < event.getSlotContext().index() && i < stacks.getSlots(); i++) {
            if (isHandWeapon(stacks.getStackInSlot(i))) return false;
        }
        return true;
    }

    @SubscribeEvent
    static void onAttributeModifier(CurioAttributeModifierEvent event) {
        if (isGauntlet(event.getItemStack())) {
            if (!firstHandWeaponSlot(event)) return;
            // what the gauntlet gives in the main hand (damage, swing speed...), now from the hands slot
            int n = 0;
            for (var entry : event.getItemStack().getAttributeModifiers().modifiers()) {
                if (!entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND)) continue;
                if (isHitAttribute(entry.attribute())) continue;
                var key = entry.attribute().unwrapKey().map(k -> k.location().getPath()).orElse("attr");
                event.addModifier(entry.attribute(), new AttributeModifier(ResourceLocation.fromNamespaceAndPath("spellclasses",
                        "gauntlet_" + BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).getPath() + "_" + key + "_" + n++
                                + "_" + event.getSlotContext().index()),
                        entry.modifier().amount(), entry.modifier().operation()));
            }
            return;
        }
        if (!COPPER_GLOVE.equals(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) return;
        event.addModifier(AttributeRegistry.LIGHTNING_SPELL_POWER, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("spellclasses", "copper_glove_storm_" + event.getSlotContext().index()),
                STORM_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static boolean isHitAttribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        return attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                || attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
    }

    private static final int HIT_SLOTS = 4;
    private static final double FIXED_DAMAGE = 16.0;
    private static final java.util.Set<ResourceLocation> CATACLYSM_FIXED_DAMAGE = java.util.Set.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_guard"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_bulwark"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_maelstrom"));

    /**
     * The hit of the worn glove or gauntlet (damage, swing speed) counts only while the main hand is empty. As a plain
     * curio modifier it also slowed the swing of a held staff or sword, and Better Combat refuses to use an item
     * while its swing cooldown is not ready, so right click with a staff did nothing.
     */
    /**
     * The three Cataclysm gauntlets show and deal 16 damage (1 + 15) wherever they are used. Done as an item attribute
     * event, so it reaches the tooltip and the main hand alike (replacing the item's components was not picked up).
     */
    @SubscribeEvent
    static void onItemAttributes(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        if (!CATACLYSM_FIXED_DAMAGE.contains(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) return;
        event.replaceModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, FIXED_DAMAGE - 1.0,
                        AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
    }

    private static final java.util.Set<ResourceLocation> SOMAKE_CAPPED_14 = java.util.Set.of(
            ResourceLocation.fromNamespaceAndPath("somakespells", "sculkborne_axe"),
            ResourceLocation.fromNamespaceAndPath("somakespells", "forbidden_hammer"));

    /** Sculkborne Axe and Forbidden Hammer hit for 14 (1 + 13), in the tooltip and in the hand. */
    @SubscribeEvent
    static void onSomakeAttributes(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        if (!SOMAKE_CAPPED_14.contains(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) return;
        event.removeAllModifiersFor(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        event.addModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, 13.0,
                        AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
    }

    @SubscribeEvent
    static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var glove = player.getMainHandItem().isEmpty() ? wornGlove(player) : net.minecraft.world.item.ItemStack.EMPTY;
        var damage = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        var speed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        if (damage == null || speed == null) return;
        var wanted = new java.util.ArrayList<AttributeModifier>();
        var damageMods = new java.util.ArrayList<AttributeModifier>();
        var speedMods = new java.util.ArrayList<AttributeModifier>();
        if (!glove.isEmpty()) {
            if (COPPER_GLOVE.equals(BuiltInRegistries.ITEM.getKey(glove.getItem()))) {
                damageMods.add(new AttributeModifier(hitId("damage", 0), ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE));
                speedMods.add(new AttributeModifier(hitId("speed", 0), ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE));
            } else {
                boolean fixedDamage = CATACLYSM_FIXED_DAMAGE.contains(BuiltInRegistries.ITEM.getKey(glove.getItem()));
                for (var entry : glove.getAttributeModifiers().modifiers()) {
                    if (!entry.slot().test(net.minecraft.world.entity.EquipmentSlot.MAINHAND)) continue;
                    if (fixedDamage && entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                        // the three Cataclysm gauntlets hit for 16 from the slot (1 + 15)
                        damageMods.clear();
                        damageMods.add(new AttributeModifier(hitId("damage", 0), FIXED_DAMAGE - 1.0, AttributeModifier.Operation.ADD_VALUE));
                        continue;
                    }
                    var list = entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) ? damageMods
                            : entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED) ? speedMods : null;
                    if (list == null || list.size() >= HIT_SLOTS) continue;
                    list.add(new AttributeModifier(hitId(list == damageMods ? "damage" : "speed", list.size()),
                            entry.modifier().amount(), entry.modifier().operation()));
                }
            }
        }
        syncHit(damage, "damage", damageMods);
        syncHit(speed, "speed", speedMods);
    }

    private static ResourceLocation hitId(String kind, int i) {
        return ResourceLocation.fromNamespaceAndPath("spellclasses", "hand_weapon_" + kind + "_" + i);
    }

    private static void syncHit(net.minecraft.world.entity.ai.attributes.AttributeInstance instance, String kind,
                                java.util.List<AttributeModifier> wanted) {
        for (int i = 0; i < HIT_SLOTS; i++) {
            ResourceLocation id = hitId(kind, i);
            if (i < wanted.size()) {
                AttributeModifier mod = wanted.get(i);
                AttributeModifier current = instance.getModifier(id);
                if (current == null || current.amount() != mod.amount() || current.operation() != mod.operation()) {
                    instance.addOrUpdateTransientModifier(mod);
                }
            } else if (instance.getModifier(id) != null) {
                instance.removeModifier(id);
            }
        }
    }

    private static final String[] BETTER_COMBAT_METHODS = {"pre_doAttack", "pre_handleBlockBreaking", "pre_Tick", "post_Tick",
            "startUpswing", "performAttack", "getCurrentHand", "cancelSwingIfNeeded", "attackFromUpswingIfNeeded", "resetComboIfNeeded",
            "updateTargets", "shouldUpdateTargetsInReach", "bettercombat", "betterCombat"};

    private static final class Cached {
        int tick = Integer.MIN_VALUE;
        net.minecraft.world.item.ItemStack stack = net.minecraft.world.item.ItemStack.EMPTY;
    }

    private static final java.util.Map<Integer, Cached> GLOVE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, Cached> GAUNTLET_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    /** The first gauntlet (a usable one: not the Copper Glove) in a hands slot, even when another glove sits in an earlier slot. */
    public static net.minecraft.world.item.ItemStack wornGauntlet(net.minecraft.world.entity.LivingEntity entity) {
        Cached cached = GAUNTLET_CACHE.computeIfAbsent(entity.getId() * 2 + (entity.level().isClientSide ? 1 : 0), k -> new Cached());
        if (cached.tick != entity.tickCount) {
            cached.tick = entity.tickCount;
            cached.stack = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(entity)
                    .flatMap(handler -> handler.findFirstCurio(CopperGloveTweak::isGauntlet))
                    .map(top.theillusivec4.curios.api.SlotResult::stack)
                    .orElse(net.minecraft.world.item.ItemStack.EMPTY);
        }
        return cached.stack;
    }

    /**
     * The Copper Glove worn in a hands slot, or an empty stack. getMainHandItem is read many times per frame, so the
     * Curios lookup is done at most once per entity per tick (separately for the client and the server side).
     */
    public static net.minecraft.world.item.ItemStack wornGlove(net.minecraft.world.entity.LivingEntity entity) {
        Cached cached = GLOVE_CACHE.computeIfAbsent(entity.getId() * 2 + (entity.level().isClientSide ? 1 : 0), k -> new Cached());
        if (cached.tick != entity.tickCount) {
            cached.tick = entity.tickCount;
            cached.stack = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(entity)
                    .flatMap(handler -> handler.findFirstCurio(CopperGloveTweak::isHandWeapon))
                    .map(top.theillusivec4.curios.api.SlotResult::stack)
                    .orElse(net.minecraft.world.item.ItemStack.EMPTY);
        }
        return cached.stack;
    }

    /** Whether the main-hand read comes from Better Combat's own code (or the methods it mixes into the game's classes). */
    public static boolean calledByBetterCombat() {
        var frames12 = StackWalker.getInstance().walk(frames -> frames.limit(12).toList());
        // The glove has nothing on right click: using an item must not see the glove as a weapon (Better Combat
        // cancels item use while a weapon is in hand)
        for (var frame : frames12) {
            String m = frame.getMethodName();
            if (m.contains("doItemUse") || m.contains("startUseItem") || m.contains("useItem")) return false;
        }
        return frames12.stream().anyMatch(frame -> {
            if (frame.getClassName().startsWith("net.bettercombat")) return true;
            String name = frame.getMethodName();
            for (String known : BETTER_COMBAT_METHODS) if (name.contains(known)) return true;
            return false;
        });
    }

    private static final ResourceLocation FIERY_JAW = ResourceLocation.fromNamespaceAndPath("legendary_monsters", "fiery_jaw");
    private static final java.util.Map<Integer, Cached> JAW_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    /** The Fiery Jaw worn in a hands slot, or an empty stack (its right click ability works from the slot). */
    public static net.minecraft.world.item.ItemStack wornJaw(net.minecraft.world.entity.LivingEntity entity) {
        Cached cached = JAW_CACHE.computeIfAbsent(entity.getId() * 2 + (entity.level().isClientSide ? 1 : 0), k -> new Cached());
        if (cached.tick != entity.tickCount) {
            cached.tick = entity.tickCount;
            cached.stack = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(entity)
                    .flatMap(handler -> handler.findFirstCurio(st -> !st.isEmpty() && FIERY_JAW.equals(BuiltInRegistries.ITEM.getKey(st.getItem()))))
                    .map(top.theillusivec4.curios.api.SlotResult::stack)
                    .orElse(net.minecraft.world.item.ItemStack.EMPTY);
        }
        return cached.stack;
    }

    /** Whether the main-hand read comes from the Fiery Jaw's own code (its use, ability and on-hit fire). */
    public static boolean calledByFieryJaw() {
        return StackWalker.getInstance().walk(frames -> frames.limit(10)
                .anyMatch(frame -> frame.getClassName().startsWith("net.miauczel.legendary_monsters")));
    }

    /** Whether the call comes from drawing the player (poses, held items): not a real use of the item. */
    public static boolean calledFromRendering() {
        return StackWalker.getInstance().walk(frames -> frames.limit(14).anyMatch(frame -> {
            String cls = frame.getClassName();
            return cls.startsWith("net.minecraft.client.renderer") || cls.startsWith("net.minecraft.client.model")
                    || cls.contains("ItemInHandLayer") || cls.contains("PlayerRenderer");
        }));
    }
}
