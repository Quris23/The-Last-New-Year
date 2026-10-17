package dev.qur1s.spellclasses.client;

import com.recolortablet.RecolorSchool;
import com.recolortablet.client.HueTextureMode;
import com.recolortablet.client.RecolorClientState;
import com.recolortablet.client.RuntimeHueTextures;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Spell icons (spell bar, wheel, books, tooltips) in the hue the player picked for the spell's school in Recolor Tablet -
 * the same hue its weapons and effects already use. Kept apart from the mixin so Recolor's classes are only loaded when
 * the mod is installed.
 */
public final class SpellIconRecolor {
    private static final String WIND_SCHOOL = "wind_spellbooks:wind";
    private static final String LIGHTNING_SCHOOL = "irons_spellbooks:lightning";

    private SpellIconRecolor() {
    }

    public static ResourceLocation recolor(AbstractSpell spell, ResourceLocation icon) {
        try {
            var player = Minecraft.getInstance().player;
            if (player == null) return icon;
            String schoolId = spell.getSchoolType().getId().toString();
            RecolorSchool school = RecolorSchool.byId(WIND_SCHOOL.equals(schoolId) ? LIGHTNING_SCHOOL : schoolId);
            if (school == null) return icon;
            float hue = RecolorClientState.chosenHue(player.getUUID(), school);
            if (Float.isNaN(hue)) return icon;
            return RuntimeHueTextures.get(icon, hue, HueTextureMode.RAMP);
        } catch (Throwable t) {
            return icon;
        }
    }
}
