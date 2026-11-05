package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Registered by SpellClasses only when Chromeware is present. The Berserk implant costs 5% Humanity for every enemy killed while it is active (Chromeware's own kill credit rules). */
public final class BerserkHumanity {
    public static final double LOSS_PER_KILL = 5.0;

    private BerserkHumanity() {
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide() || !net.neoforged.fml.ModList.get().isLoaded("chromeware")) return;
        try {
            Player p = EnemyKills.credit(event);
            if (p == null || !ZetatechBerserk.active(p)) return;
            HumanityState s = Humanity.state(p);
            p.setData(Humanity.STATE, s.with(s.permanent() + LOSS_PER_KILL, s.temporary()));
            Humanity.strain(p, 1.0E-6); // only to push the new value to the client
        } catch (Throwable ignored) {
        }
    }
}
