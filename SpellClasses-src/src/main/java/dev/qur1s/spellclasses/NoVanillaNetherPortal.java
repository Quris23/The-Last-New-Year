package dev.qur1s.spellclasses;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * The obsidian Nether portal is removed on purpose: the way down is the Rift Gate's built-in "Ад" destination
 * (see {@link dev.qur1s.spellclasses.compat.NetherGate}). Lighting an obsidian frame no longer creates a portal;
 * portals that already exist stop working too (see {@code NetherPortalBlockMixin}).
 */
@EventBusSubscriber(modid = "spellclasses")
public final class NoVanillaNetherPortal {
    private NoVanillaNetherPortal() {
    }

    @SubscribeEvent
    static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }
}
