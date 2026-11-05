package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qur1s.spellclasses.compat.JeiMultilingualNames;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IListElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI's name search also matches the item names in Language Reload's fallback languages (like the creative search does).
 * Wraps the calls to {@code createNames} rather than injecting into it: its body reuses the local slot of {@code this}.
 */
@Mixin(targets = "mezz.jei.gui.ingredients.ListElementInfo", remap = false)
public abstract class JeiMultilingualSearchMixin {
    @Shadow
    @Final
    private IListElement<?> element;

    @WrapOperation(method = {"<init>", "refreshIngredientAliases"}, require = 0, remap = false, at = @At(value = "INVOKE",
            target = "Lmezz/jei/gui/ingredients/ListElementInfo;createNames(Ljava/lang/String;Lmezz/jei/api/runtime/IIngredientManager;Lmezz/jei/common/config/IIngredientFilterConfig;)Ljava/util/List;",
            remap = false))
    private List<String> spellclasses$otherLanguages(mezz.jei.gui.ingredients.ListElementInfo<?> self, String displayName,
                                                      IIngredientManager manager, IIngredientFilterConfig config,
                                                      com.llamalad7.mixinextras.injector.wrapoperation.Operation<List<String>> original) {
        List<String> names = original.call(self, displayName, manager, config);
        List<String> extra = JeiMultilingualNames.otherLanguages(element.getTypedIngredient(), manager, names);
        if (extra.isEmpty()) return names;
        List<String> all = new ArrayList<>(names);
        all.addAll(extra);
        return all;
    }
}
