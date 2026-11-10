package lny.cinematicoptions;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Volume of the pack's loading screen trailer (lny-cinematic, played by the early loading window), on Minecraft's sound
 * options screen. It is kept in options.txt under {@link #KEY}, which is where the early window reads it from at the next
 * start, together with the master volume (the trailer plays at master times this).
 */
public class LoadingScreenVolume {
   public static final String KEY = "lny_cinematicVolume";
   public static final OptionInstance<Double> OPTION = new OptionInstance<>(
      "options.lny_cinematic.loadingScreenVolume",
      OptionInstance.cachedConstantTooltip(Component.translatable("options.lny_cinematic.loadingScreenVolume.tooltip")),
      (caption, value) -> value == 0.0
         ? Options.genericValueLabel(caption, CommonComponents.OPTION_OFF)
         : Component.translatable("options.percent_value", caption, (int)(value * 100.0)),
      OptionInstance.UnitDouble.INSTANCE,
      1.0,
      value -> {
      }
   );
}
