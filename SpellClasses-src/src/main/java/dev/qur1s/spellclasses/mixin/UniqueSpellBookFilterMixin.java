package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.NecronomiconSpells;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.NecronomiconSpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * {@code UniqueSpellBook.getSpells()} is the single source of truth for a preset spellbook's
 * fixed loadout - both its tooltip and the per-stack spell_container it seeds on first touch
 * ({@code initializeSpellContainer}) read from it, so it's the only reliable place to change what
 * a preset book carries (the per-stack data component can't be overridden after the fact: once
 * set it's guarded by an "already a spell container" check, and casting/tooltips for these
 * "Unique" items go through this method rather than the mutable component anyway).
 * <p>
 * Two unrelated overrides live here since both need this same hook:
 * <ul>
 *   <li>Cataclysm: Spellbooks' Desert Spell Book hardcodes Desert Winds as a preset spell, but
 *   Desert Winds is disabled (version-mismatch crash, see desert_winds.json) - stripped from every
 *   preset book's loadout.</li>
 *   <li>The Necronomicon's own 4 hardcoded spells are replaced entirely with the pack's full
 *   creature-summon roster - see {@link NecronomiconSpells}.</li>
 * </ul>
 */
@Mixin(value = UniqueSpellBook.class, remap = false)
public abstract class UniqueSpellBookFilterMixin {
    private static final ResourceLocation DESERT_WINDS = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "desert_winds");

    @Shadow
    List<SpellData> spellData;

    @Inject(method = "getSpells", at = @At("RETURN"), cancellable = true)
    private void spellclasses$overrideSpells(CallbackInfoReturnable<List<SpellData>> cir) {
        List<SpellData> result;
        if ((Object) this instanceof NecronomiconSpellBook) {
            result = NecronomiconSpells.summonSpellData();
        } else {
            result = cir.getReturnValue().stream()
                    .filter(data -> !data.getSpell().getSpellResource().equals(DESERT_WINDS))
                    .toList();
        }
        this.spellData = result;
        cir.setReturnValue(result);
    }
}
