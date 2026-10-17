package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Full set of Scarecrow armor (irons_spellbooks:pumpkin_*): hitting with Blood-school spells restores hunger, and Blood Step
 * reaches 10 blocks further (see BloodStepDistanceMixin).
 */
@EventBusSubscriber(modid = "spellclasses")
public final class ScarecrowArmor {
    private ScarecrowArmor() {
    }

    private static final Map<EquipmentSlot, ResourceLocation> SET = Map.of(
            EquipmentSlot.HEAD, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "pumpkin_helmet"),
            EquipmentSlot.CHEST, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "pumpkin_chestplate"),
            EquipmentSlot.LEGS, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "pumpkin_leggings"),
            EquipmentSlot.FEET, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "pumpkin_boots"));

    /** Hunger points restored per Blood spell hit, and the minimum gap between two restorations, in ticks. */
    private static final int HUNGER_POINTS = 1;
    private static final float SATURATION = 0.5f;
    private static final int GAP_TICKS = 10;
    /** Extra Blood Step distance with the set, in blocks. */
    public static final float BLOOD_STEP_BONUS = 10.0f;

    private static final Map<UUID, Integer> LAST_RESTORE = new WeakHashMap<>();

    public static boolean wearsFullSet(LivingEntity entity) {
        for (var entry : SET.entrySet()) {
            ResourceLocation worn = BuiltInRegistries.ITEM.getKey(entity.getItemBySlot(entry.getKey()).getItem());
            if (!worn.equals(entry.getValue())) return false;
        }
        return true;
    }

    @SubscribeEvent
    static void onSpellHit(LivingDamageEvent.Post event) {
        if (!(event.getSource() instanceof SpellDamageSource spellSource)) return;
        if (!(spellSource.getEntity() instanceof ServerPlayer player)) return;
        var spell = spellSource.spell();
        if (spell == null || !SchoolRegistry.BLOOD.get().equals(spell.getSchoolType())) return;
        if (!wearsFullSet(player)) return;
        Integer last = LAST_RESTORE.get(player.getUUID());
        if (last != null && player.tickCount - last < GAP_TICKS && player.tickCount >= last) return;
        LAST_RESTORE.put(player.getUUID(), player.tickCount);
        player.getFoodData().eat(HUNGER_POINTS, SATURATION);
    }
}
