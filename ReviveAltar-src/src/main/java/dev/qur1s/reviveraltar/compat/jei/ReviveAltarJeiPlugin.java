package dev.qur1s.reviveraltar.compat.jei;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * Only loaded by JEI itself (via its own @JeiPlugin scanning) when JEI is actually
 * installed, so this class is never touched if it isn't present.
 */
@JeiPlugin
public class ReviveAltarJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReviveAltarMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }
}
