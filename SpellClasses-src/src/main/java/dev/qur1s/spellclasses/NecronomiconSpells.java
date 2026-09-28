package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The Necronomicon (irons_spellbooks:necronomicon_spell_book) ships with 4 hardcoded spells
 * (blood_slash/blood_step/ray_of_siphoning/blaze_storm) baked into Iron's Spellbooks' own
 * {@code NecronomiconSpellBook} Java constructor. {@link dev.qur1s.spellclasses.mixin.UniqueSpellBookFilterMixin}
 * replaces that fixed loadout with the pack's full creature-summon roster instead. The matching
 * cast-permission exception (only a Vampire/blood-class player may cast these, and only while
 * holding this specific book) lives in {@link GateEvents}.
 */
public final class NecronomiconSpells {
    private NecronomiconSpells() {
    }

    public static final ResourceLocation NECRONOMICON = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "necronomicon_spell_book");

    // Conjure Amethyst Crab (Nature) and Thoth's Witness (Sand) were deliberately dropped - the
    // Vampire shouldn't get access to those. Conjure Ignited Reinforcement (Fire) stays.
    private static final List<Supplier<AbstractSpell>> SUMMON_SPELLS = List.of(
            SpellRegistry.SUMMON_VEX_SPELL,
            SpellRegistry.SUMMON_POLAR_BEAR_SPELL,
            SpellRegistry.RAISE_DEAD_SPELL,
            SpellRegistry.SUMMON_HORSE_SPELL,
            SpellRegistries.CONJURE_KOBOLDIATOR,
            SpellRegistries.CONJURE_KOBOLETON,
            SpellRegistries.CONJURE_THRALL,
            SpellRegistries.CONJURE_IGNITED_REINFORCEMENT
    );

    /** Same list as {@link #SUMMON_SPELLS}, as the spell-id strings {@code SpellPreCastEvent} deals in - see {@link GateEvents}. */
    public static final Set<String> SUMMON_SPELL_IDS = Set.of(
            "irons_spellbooks:summon_vex",
            "irons_spellbooks:summon_polar_bear",
            "irons_spellbooks:raise_dead",
            "irons_spellbooks:summon_horse",
            "cataclysm_spellbooks:conjure_koboldiator",
            "cataclysm_spellbooks:summon_koboleton",
            "cataclysm_spellbooks:conjure_thralls",
            "cataclysm_spellbooks:conjure_ignited_reinforcement"
    );

    /** Built fresh each call since {@code getSpells()} may be invoked before the spell registries are fully populated. */
    public static List<SpellData> summonSpellData() {
        return SUMMON_SPELLS.stream()
                .map(spell -> new SpellData(spell.get(), 5, true))
                .collect(Collectors.toList());
    }

    /** True if the player has the Necronomicon in hand (either hand) or worn in a Curios book/artifact slot. */
    public static boolean isHolding(Player player) {
        Item necronomicon = BuiltInRegistries.ITEM.get(NECRONOMICON);
        if (is(player.getMainHandItem(), necronomicon) || is(player.getOffhandItem(), necronomicon)) return true;
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(necronomicon).isPresent())
                .orElse(false);
    }

    /** The Vampire class's own armor set (irons_spellbooks:cultist_*) - same as the Blood chapter's armor quest. */
    private static final Map<EquipmentSlot, ResourceLocation> BLOOD_ARMOR_SET = Map.of(
            EquipmentSlot.HEAD, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cultist_helmet"),
            EquipmentSlot.CHEST, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cultist_chestplate"),
            EquipmentSlot.LEGS, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cultist_leggings"),
            EquipmentSlot.FEET, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cultist_boots")
    );

    /** True if the player is wearing the full Blood/Cultist armor set - required alongside {@link #isHolding} to cast a summon spell. */
    public static boolean wearsBloodArmor(Player player) {
        for (var entry : BLOOD_ARMOR_SET.entrySet()) {
            ResourceLocation worn = BuiltInRegistries.ITEM.getKey(player.getItemBySlot(entry.getKey()).getItem());
            if (!worn.equals(entry.getValue())) return false;
        }
        return true;
    }

    private static boolean is(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }
}
