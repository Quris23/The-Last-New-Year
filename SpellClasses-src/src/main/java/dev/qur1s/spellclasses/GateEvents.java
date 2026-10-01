package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class GateEvents {
    private GateEvents() {
    }

    // Brontes and the Void Forge (see PresetWeaponSpells) are plain Cataclysm weapons, not
    // SwordItem and not natively IPresetSpellContainer, so they fall outside the check below.
    // Their forced-imbued spell is meant for whoever holds the weapon, regardless of class.
    private static final Set<ResourceLocation> UNIVERSAL_PRESET_WEAPONS = Set.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm", "brontes"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "void_forge")
    );

    /**
     * Thoth's Witness, Conjure Koboldiator, and Conjure/Summon Koboleton are all themed entirely
     * around Pharaoh/Sand, require the full Pharaoh Mage set to cast (see
     * {@link dev.qur1s.spellclasses.mixin.AbstractSpellOverridesMixin}), and live in Cataclysm:
     * Spellbooks' own {@code spells.holy} Java package - but the mod itself registers all three under
     * Iron's Spellbooks' real {@code holy} school (confirmed by decompiling each spell's constructor),
     * not {@code cataclysm_spellbooks:sand}. Left as-is, the generic class gate below reads their real
     * school and demands the Holy class ("Священник") before the Pharaoh-armor check ever runs.
     * Substituting the Sand school here (already in {@link ClassSchools#ADDON_FREE_SCHOOLS}) for these
     * three spell ids only skips that misattributed class requirement, leaving every other Holy-school
     * spell gated normally.
     */
    private static final Set<ResourceLocation> PHARAOH_HOLY_MISLABELED_SPELLS = Set.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "thoths_witness"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "conjure_koboldiator"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "summon_koboleton")
    );
    private static final ResourceLocation SAND_SCHOOL = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "sand");

    @SubscribeEvent
    static void onPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();
        var mainHandItem = player.getMainHandItem().getItem();

        if (event.getCastSource() == CastSource.SWORD) {
            // Swords built by the game around one fixed spell (Spellbreaker's Counterspell, Misery's
            // Wither Skull, ...) work for whoever's holding them, regardless of class. A plain sword
            // a player imbued themselves at the Arcane Anvil doesn't carry that marker, so it's still
            // gated normally — imbuing can't be used to hand a class-locked spell to someone else.
            // Must also actually be a SwordItem: non-sword preset items (Hither-Thither Wand, ...) cast
            // through the same CastSource.SWORD path but should stay gated by class like everything else.
            if (mainHandItem instanceof IPresetSpellContainer && mainHandItem instanceof SwordItem) {
                return;
            }
            if (UNIVERSAL_PRESET_WEAPONS.contains(BuiltInRegistries.ITEM.getKey(mainHandItem))) {
                return;
            }
        }

        var schoolId = PHARAOH_HOLY_MISLABELED_SPELLS.contains(event.getSpellId()) ? SAND_SCHOOL : event.getSchoolType().getId();
        var chosen = player instanceof ServerPlayer sp ? ClassManager.chosenSchool(sp).orElse(null) : null;
        if (ClassSchools.isAllowed(schoolId, chosen)) return;

        // The Necronomicon's summon spells (see NecronomiconSpells) span several schools by
        // design - a Vampire carrying the book (hand or Curios artifact slot) and wearing the
        // full Blood/Cultist armor set may cast any of them regardless of school.
        if (chosen != null && chosen.equals(SchoolRegistry.BLOOD_RESOURCE)
                && NecronomiconSpells.SUMMON_SPELL_IDS.contains(event.getSpellId())
                && NecronomiconSpells.isHolding(player)) {
            if (NecronomiconSpells.wearsBloodArmor(player)) return;

            event.setCanceled(true);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("spellclasses.message.requires_blood_armor").withStyle(ChatFormatting.RED));
            }
            return;
        }

        event.setCanceled(true);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("spellclasses.message.wrong_school",
                    ClassSchools.className(schoolId)).withStyle(ChatFormatting.RED));
        }
    }

    // Spells removed/broken by a mod update can leave a player permanently stuck with
    // isCasting=true in their saved MagicData; MagicManager.tick() then retries that cast
    // every server tick forever, crashing the world on every single load. Clearing it here
    // runs before the level starts ticking, so the world gets a chance to actually load.
    private static final ResourceLocation[] STUCK_CAST_RECOVERY_SPELLS = {
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "desert_winds")
    };

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData.isCasting()) {
            String castingSpellId = magicData.getCastingSpellId();
            for (ResourceLocation stuck : STUCK_CAST_RECOVERY_SPELLS) {
                if (stuck.toString().equals(castingSpellId)) {
                    magicData.resetCastingState();
                    break;
                }
            }
        }

        ColdSweatCompat.syncResistance(player, ClassManager.chosenSchool(player));
        if (ClassManager.hasChosen(player) || ClassManager.bookGiven(player)) return;
        ClassManager.giveClassBook(player);
    }
}
