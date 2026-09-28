package dev.qur1s.spellclasses.client;

import net.minecraft.client.Minecraft;

/**
 * Split out from {@link dev.qur1s.spellclasses.item.ClassSelectionItem} so the client-only
 * {@code Minecraft}/{@code Screen} references only get resolved when this class is actually
 * loaded — which only happens inside an {@code isClientSide()} branch, never on a dedicated server.
 */
public final class ClientScreenOpener {
    private ClientScreenOpener() {
    }

    public static void openClassSelection() {
        Minecraft.getInstance().setScreen(new ClassSelectionScreen());
    }
}
