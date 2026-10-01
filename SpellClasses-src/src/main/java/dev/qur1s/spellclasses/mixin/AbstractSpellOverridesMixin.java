package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.blood.BloodSlashSpell;
import net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKoboldiatorSpell;
import net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ConjureKoboletonSpell;
import net.acetheeldritchking.cataclysm_spellbooks.spells.holy.ThothsWitnessSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Per-spell overrides that can't be done by overriding a method in the spell's own class, because
 * that method is only ever declared once, in {@link AbstractSpell} itself, and each spell just sets
 * plain int fields (base/per-level power, mana cost, ...) that the shared method reads.
 */
@Mixin(value = AbstractSpell.class, remap = false)
public abstract class AbstractSpellOverridesMixin {
    private static final int BLOOD_SLASH_MANA_DIVISOR = 10;

    /** Blood Slash's stock damage curve, for rescaling the multiplier-adjusted result below. */
    private static final float BLOOD_SLASH_STOCK_BASE = 10.0f;
    private static final float BLOOD_SLASH_STOCK_PER_LEVEL = 1.0f;
    private static final float BLOOD_SLASH_NEW_BASE = 1.7f;
    private static final float BLOOD_SLASH_NEW_PER_LEVEL = 0.2f;

    @Inject(method = "getManaCost", at = @At("RETURN"), cancellable = true)
    private void spellclasses$cheaperBloodSlash(int spellLevel, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof BloodSlashSpell) {
            cir.setReturnValue(Math.max(1, cir.getReturnValue() / BLOOD_SLASH_MANA_DIVISOR));
        }
    }

    @Inject(method = "getSpellPower", at = @At("RETURN"), cancellable = true)
    private void spellclasses$bloodSlashDamageCurve(int spellLevel, @Nullable Entity sourceEntity, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof BloodSlashSpell) {
            float stockBase = BLOOD_SLASH_STOCK_BASE + BLOOD_SLASH_STOCK_PER_LEVEL * (spellLevel - 1);
            float newBase = BLOOD_SLASH_NEW_BASE + BLOOD_SLASH_NEW_PER_LEVEL * (spellLevel - 1);
            cir.setReturnValue(cir.getReturnValue() * (newBase / stockBase));
        }
    }

    private static final ResourceLocation SAND_SCHOOL = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "sand");
    private static final ResourceLocation ABYSSAL_SCHOOL = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal");

    /** Per-slot acceptable item IDs for a full-set requirement; HEAD has two alternatives for Abyssal (hood or mask). */
    private static final java.util.Map<EquipmentSlot, ResourceLocation[]> PHARAOH_MAGE_SET = java.util.Map.of(
            EquipmentSlot.HEAD, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "pharaoh_helmet")},
            EquipmentSlot.CHEST, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "pharaoh_chestplate")},
            EquipmentSlot.LEGS, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "pharaoh_leggings")},
            EquipmentSlot.FEET, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "pharaoh_greaves")}
    );

    private static final java.util.Map<EquipmentSlot, ResourceLocation[]> ABYSSAL_WARLOCK_SET = java.util.Map.of(
            EquipmentSlot.HEAD, new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_warlock_helmet"),
                    ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_warlock_mask")
            },
            EquipmentSlot.CHEST, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_warlock_chestplate")},
            EquipmentSlot.LEGS, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_warlock_leggings")},
            EquipmentSlot.FEET, new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_warlock_boots")}
    );

    private static boolean wearingFullSet(Player player, java.util.Map<EquipmentSlot, ResourceLocation[]> set) {
        for (var entry : set.entrySet()) {
            ResourceLocation worn = BuiltInRegistries.ITEM.getKey(player.getItemBySlot(entry.getKey()).getItem());
            boolean matches = false;
            for (ResourceLocation accepted : entry.getValue()) {
                if (worn.equals(accepted)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }
        return true;
    }

    /**
     * Cataclysm: Spellbooks' Thoth's Witness already requires Pharaoh Mage armor worn to cast;
     * apply a full-set requirement to every Sand-school spell, and the equivalent Abyssal Warlock
     * full set to every Abyssal-school spell (neither school had this hardcoded elsewhere).
     */
    @Inject(method = "canBeCastedBy", at = @At("RETURN"), cancellable = true)
    private void spellclasses$schoolArmorRequirement(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        AbstractSpell self = (AbstractSpell) (Object) this;
        if (!cir.getReturnValue().isSuccess()) return;

        ResourceLocation schoolId = self.getSchoolType().getId();
        java.util.Map<EquipmentSlot, ResourceLocation[]> requiredSet;
        String messageKey;
        // Thoth's Witness, Conjure Koboldiator, and Conjure Koboleton all live in the mod's own
        // "spells.holy" package and are registered under Iron's Spellbooks' real "holy" school
        // (not cataclysm_spellbooks:sand, despite the Pharaoh/Sand theming), so schoolId alone misses
        // all three here.
        if (schoolId.equals(SAND_SCHOOL) || self instanceof ThothsWitnessSpell
                || self instanceof ConjureKoboldiatorSpell || self instanceof ConjureKoboletonSpell) {
            requiredSet = PHARAOH_MAGE_SET;
            messageKey = "spellclasses.message.requires_pharaoh_armor";
        } else if (schoolId.equals(ABYSSAL_SCHOOL)) {
            requiredSet = ABYSSAL_WARLOCK_SET;
            messageKey = "spellclasses.message.requires_abyssal_armor";
        } else {
            return;
        }

        if (!wearingFullSet(player, requiredSet)) {
            cir.setReturnValue(new CastResult(CastResult.Type.FAILURE,
                    Component.translatable(messageKey).withStyle(ChatFormatting.RED)));
        }
    }
}
