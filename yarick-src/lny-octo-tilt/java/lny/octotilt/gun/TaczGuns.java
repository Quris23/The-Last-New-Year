package lny.octotilt.gun;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** The calls into TaCZ, kept apart so nothing of it is loaded without the mod */
final class TaczGuns {
   private TaczGuns() {
   }

   static ItemStack create(ResourceLocation gunId, HolderLookup.Provider registries) {
      return GunItemBuilder.create().setId(gunId).setFireMode(FireMode.AUTO).setAmmoCount(0).forceBuild(registries);
   }

   static boolean is(ItemStack stack, ResourceLocation gunId) {
      IGun gun = IGun.getIGunOrNull(stack);
      return gun != null && gunId.equals(gun.getGunId(stack));
   }
}
