package dev.qur1s.spellclasses.mixin;

import com.github.L_Ender.cataclysm.items.Brontes;
import com.github.L_Ender.cataclysm.items.Infernal_forge;
import com.github.L_Ender.cataclysm.items.Void_forge;
import net.acetheeldritchking.cataclysm_spellbooks.items.weapons.HellfireForgeItem;
import net.mcreator.borninchaosv.item.SkullCrusherItem;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * Gives Brontes, the Infernal Forge, the Void Forge (Cataclysm), the Hellfire Forge (Cataclysm:
 * Spellbooks) and the Skullbreaker Hammer (Born in Chaos) the vanilla Mace's "smash attack" landing
 * effects (knockback, no-fall-damage stop, the mace sounds) - the exact logic from
 * {@link net.minecraft.world.item.MaceItem#hurtEnemy}, added onto each item's own hurtEnemy
 * override without touching its existing on-hit effects. The matching bonus-damage half -
 * {@code Item.getAttackDamageBonus}/{@code postHurtEnemy}, which none of them override - lives in
 * {@link ItemMaceSmashMixin}.
 */
@Mixin(value = {Brontes.class, Infernal_forge.class, Void_forge.class, SkullCrusherItem.class, HellfireForgeItem.class}, remap = false)
public abstract class CataclysmMaceSmashMixin {
    @Inject(method = "hurtEnemy", at = @At("RETURN"))
    private void spellclasses$maceSmash(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof ServerPlayer serverPlayer) || !MaceItem.canSmashAttack(serverPlayer)) return;
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;

        serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().with(Direction.Axis.Y, 0.01F));
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));

        SoundEvent sound;
        if (target.onGround()) {
            sound = serverPlayer.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
        } else {
            sound = SoundEvents.MACE_SMASH_AIR;
        }
        serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), sound, serverPlayer.getSoundSource(), 1.0F, 1.0F);

        knockback(serverLevel, serverPlayer, target);
    }

    private static void knockback(ServerLevel level, Player player, LivingEntity entity) {
        level.levelEvent(2013, entity.getOnPos(), 750);

        Predicate<LivingEntity> targets = other -> !other.isSpectator()
                && other != player
                && other != entity
                && !player.isAlliedTo(other)
                && !(other instanceof TamableAnimal tamable && tamable.isTame() && player.getUUID().equals(tamable.getOwnerUUID()))
                && !(other instanceof ArmorStand armorStand && armorStand.isMarker())
                && entity.distanceToSqr(other) <= 3.5 * 3.5;

        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5), targets).forEach(other -> {
            Vec3 offset = other.position().subtract(entity.position());
            double power = (3.5 - offset.length()) * 0.7F * (player.fallDistance > 5.0F ? 2 : 1)
                    * (1.0 - other.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            if (power <= 0.0) return;

            Vec3 push = offset.normalize().scale(power);
            other.push(push.x, 0.7F, push.z);
            if (other instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        });
    }
}
