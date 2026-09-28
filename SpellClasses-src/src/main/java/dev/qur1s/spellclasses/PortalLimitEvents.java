package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * The Portal spell (entity-based portals, not the Portal Frame block) allows only one live pair
 * at a time: starting a new pair discards whatever pair (or dangling single portal) the caster
 * already had out, so a fresh cast never leaves more than the two just-placed portals standing.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class PortalLimitEvents {
    private PortalLimitEvents() {
    }

    @SubscribeEvent
    static void onSpellCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getSpellId().equals(SpellRegistry.PORTAL_SPELL.get().getSpellId())) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData.getPlayerRecasts().hasRecastForSpell(event.getSpellId())) {
            // Second cast of the current pair — nothing to evict, let it complete normally.
            return;
        }

        var server = player.getServer();
        if (server == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            for (PortalEntity portal : level.getEntities(EntityRegistry.PORTAL.get(), portal -> player.getUUID().equals(portal.getOwnerUUID()))) {
                portal.discard();
                PortalManager.INSTANCE.killPortal(portal.getUUID(), player.getUUID());
            }
        }
    }
}
