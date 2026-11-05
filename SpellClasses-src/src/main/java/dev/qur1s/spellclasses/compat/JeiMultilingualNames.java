package dev.qur1s.spellclasses.compat;

import com.euphony.neo_language_reload.access.ILanguage;
import com.euphony.neo_language_reload.access.ITranslationStorage;
import com.euphony.neo_language_reload.config.Config;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Display names of an ingredient in Language Reload's fallback languages, so JEI finds it by any of them. */
public final class JeiMultilingualNames {
    private JeiMultilingualNames() {
    }

    public static <V> List<String> otherLanguages(ITypedIngredient<V> typed, IIngredientManager manager, List<String> known) {
        if (!ModList.get().isLoaded("neo_language_reload")) return List.of();
        Config config = Config.getInstance();
        if (!config.multilingualItemSearch || config.fallbacks.isEmpty()) return List.of();
        if (!(Language.getInstance() instanceof ILanguage language)) return List.of();
        var storage = language.languagereload_getTranslationStorage();
        if (!(storage instanceof ITranslationStorage translations)) return List.of();

        List<String> extra = new ArrayList<>();
        var helper = manager.getIngredientHelper(typed.getType());
        String previous = translations.languagereload_getTargetLanguage();
        try {
            for (String code : config.fallbacks) {
                translations.languagereload_setTargetLanguage(code);
                String name = ChatFormatting.stripFormatting(helper.getDisplayName(typed.getIngredient()));
                if (name == null || name.isEmpty()) continue;
                name = name.toLowerCase(Locale.ROOT);
                if (!known.contains(name) && !extra.contains(name)) extra.add(name);
            }
        } catch (RuntimeException ignored) {
            // an ingredient with a broken name must not break JEI's index
        } finally {
            translations.languagereload_setTargetLanguage(previous);
        }
        return extra;
    }
}
