package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.implant.ImplantAccess;
import dev.qur1s.spellclasses.implant.ImplantBackpackItem;
import dev.qur1s.spellclasses.network.ImplantUpgradePayload;
import net.minecraft.client.Minecraft;
import dev.qur1s.spellclasses.implant.ImplantBackpack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import dev.qur1s.spellclasses.implant.ImplantUpgrades;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The inventory screen gets the buttons of the backpack upgrades installed as implants: a column of slots on the left (click to
 * open its settings), clicking a button opens a small settings panel of the upgrade (to the right of the inventory) and every Tank upgrade as a vertical fluid tube left of the button column.
 */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class ImplantUpgradeButtons {
    private static final int LEFT_SIZE = 22;
    private static final int TAB_SIZE = 26;
    private static final int GAP = 2;

    private ImplantUpgradeButtons() {
    }

    private enum Kind { TOGGLE, TAB, TUBE, PANEL, BG }

    private record Box(int x, int y, int w, int h, int category, int socket, Kind kind, ItemStack stack, int action) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static final int TUBE_W = 20;
    private static final int TUBE_H = 96;
    private static final int TUBE_PITCH = 34;
    private static final int PANEL_W = 100;
    private static final ResourceLocation STORAGE_ON = ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/gui/implant_storage_on.png");
    private static final ResourceLocation STORAGE_OFF = ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/gui/implant_storage_off.png");
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath("sophisticatedcore", "textures/gui/icons.png");

    /** Actions of ImplantUpgradePayload: 0 on/off, 1 receive/give, 2 world, 3 storages, 4 tank in hand. */
    private static final int ON_OFF = 0, DIRECTION = 1, WORLD = 2, STORAGES = 3, HAND = 4;

    /** The tab whose panel is open: category * 16 + socket, or -1. */
    private static int openTab = -1;

    private static boolean isTank(ItemStack stack) {
        return stack.getItem() instanceof TankUpgradeItem;
    }

    private static boolean isFluidPump(ItemStack stack) {
        return stack.getItem() instanceof PumpUpgradeItem;
    }

    private static boolean isOwnPump(ItemStack stack) {
        return stack.getItem() == ImplantBackpackItem.BLOOD_PUMP.get() || stack.getItem() == ImplantBackpackItem.MANA_PUMP.get();
    }

    /** The setting buttons of an item's panel: Sophisticated's pumps have all four, our blood and mana pumps just the on/off button. */
    private static int[] settings(ItemStack stack) {
        if (isFluidPump(stack)) return new int[]{DIRECTION, WORLD, STORAGES, HAND};
        return new int[0];
    }

    private static boolean supported(Object screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static boolean flag(ItemStack stack, java.util.function.Supplier<net.minecraft.core.component.DataComponentType<Boolean>> type, boolean fallback) {
        return stack.getOrDefault(type, fallback);
    }

    private static List<Box> boxes(AbstractContainerScreen<?> screen) {
        List<Box> result = new ArrayList<>();
        var player = Minecraft.getInstance().player;
        if (player == null) return result;
        List<ImplantAccess.Entry> listed = ImplantAccess.listed(player);
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        int rx = left + screen.getXSize() + 3;
        // the tubes stand left of the column of upgrade buttons, one more to the left for every Tank
        // with no button column the tubes stand as far from the inventory as from each other
        boolean anyButton = listed.stream().anyMatch(e -> !isTank(e.stack()) && ImplantUpgrades.forBackpack(e.stack()));
        int tubeRight = left - (anyButton ? LEFT_SIZE + 4 + 8 : TUBE_PITCH - TUBE_W);
        int tank = 0;
        int toggles = 0;
        for (var entry : listed) {
            ItemStack stack = entry.stack();
            if (isTank(stack)) {
                result.add(new Box(tubeRight - TUBE_W - tank * TUBE_PITCH, top + 4, TUBE_W, TUBE_H, entry.category(), entry.index(), Kind.TUBE, stack, 0));
                tank++;
                continue;
            }
            if (!ImplantUpgrades.forBackpack(stack)) continue; // blood, mana and XP pumps have no button: they always work
            result.add(new Box(left - LEFT_SIZE - 4, top + 4 + toggles * (LEFT_SIZE + GAP), LEFT_SIZE, LEFT_SIZE,
                    entry.category(), entry.index(), Kind.TOGGLE, stack, 0));
            toggles++;
            if (openTab == entry.category() * 16 + entry.index()) {
                int[] settings = settings(stack);
                int ph = 22 + (settings.length > 0 ? 22 : 0) + 20 + 2;
                int px = left + 2;
                int py = Math.max(2, top + 4 + (toggles - 1) * (LEFT_SIZE + GAP));
                result.add(new Box(px, py, PANEL_W, ph, entry.category(), entry.index(), Kind.BG, stack, 0));
                int rowY = py + 22;
                if (settings.length > 0) {
                    int i = 0;
                    for (int action : settings) {
                        result.add(new Box(px + 4 + i * 23, rowY, 20, 20, entry.category(), entry.index(), Kind.PANEL, stack, action));
                        i++;
                    }
                    rowY += 22;
                }
                result.add(new Box(px + 4, rowY, PANEL_W - 8, 16, entry.category(), entry.index(), Kind.PANEL, stack, ON_OFF));
            }
        }
        return result;
    }

    private static boolean enabled(ItemStack stack) {
        return stack.getOrDefault(ModCoreDataComponents.ENABLED, true);
    }

    private static String panelLabel(Box box) {
        ItemStack st = box.stack();
        boolean pump = isFluidPump(st);
        PumpUpgradeItem item = pump ? (PumpUpgradeItem) st.getItem() : null;
        return switch (box.action()) {
            case ON_OFF -> enabled(st) ? "Включено" : "Выключено";
            case DIRECTION -> flag(st, ModCoreDataComponents.IS_INPUT, true) ? "Принимать" : "Отдавать";
            case WORLD -> "Мир: " + onOff(flag(st, ModCoreDataComponents.INTERACT_WITH_WORLD, item != null && item.getInteractWithWorldDefault()));
            case STORAGES -> "Хранилища: " + onOff(flag(st, ModCoreDataComponents.INTERACT_WITH_FLUID_HANDLERS,
                    item != null && item.getInteractWithFluidHandlersDefault()));
            case HAND -> "Ёмкость в руке: " + onOff(flag(st, ModCoreDataComponents.INTERACT_WITH_HAND,
                    item != null && item.getInteractWithHandDefault()));
            default -> "";
        };
    }

    private static String onOff(boolean value) {
        return value ? "да" : "нет";
    }

    private static int capacity(ItemStack stack, int amount) {
        try {
            var player = Minecraft.getInstance().player;
            var wrapper = BackpackWrapper.fromStack(ImplantBackpack.stack(player));
            int capacity = ((TankUpgradeItem) stack.getItem()).getTankCapacity(wrapper);
            if (capacity > 0) return capacity;
        } catch (Throwable ignored) {
        }
        return Math.max(4000, amount);
    }

    private static void drawFluid(GuiGraphics g, FluidStack fluid, int x, int y, int w, int h) {
        var ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
        int tint = ext.getTintColor(fluid);
        float r = (tint >> 16 & 255) / 255f, gr = (tint >> 8 & 255) / 255f, b = (tint & 255) / 255f;
        g.enableScissor(x, y, x + w, y + h);
        for (int yy = y; yy < y + h; yy += 16) {
            for (int xx = x; xx < x + w; xx += 16) g.blit(xx, yy, 0, 16, 16, sprite, r, gr, b, 1f);
        }
        g.disableScissor();
    }

    /** The frame colour of the buttons and the panel: black (the gold of the first version is gone). */
    private static final int GOLD = 0xFF000000;
    /** Colours of the inventory theme: panel, slot and slot edge. */
    private static final int PANEL = 0xFF8E5A4C;
    private static final int SLOT = 0xFF72423A;
    private static final int SLOT_EDGE = 0xFF4A2823;
    private static final int GOLD_LIGHT = 0xFF000000;
    private static final int TUBE_DARK = 0xFF35201C;
    private static final int TUBE_EDGE_LIGHT = 0xFFB07C6C;
    private static final int TUBE_TICK = 0xFFC99484;

    /** The same gold-framed gauge tube Sophisticated draws: dark inside, gold ticks along both long edges, fluid rising from the bottom. */
    private static void drawTube(GuiGraphics g, Box box, Font font) {
        var fluid = tankFluid(box.stack());
        int amount = fluid == null ? 0 : fluid.getAmount();
        int capacity = capacity(box.stack(), amount);
        int x = box.x(), y = box.y(), w = box.w(), h = box.h();
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w, y + 1, TUBE_EDGE_LIGHT);
        g.fill(x, y + h - 1, x + w, y + h, SLOT_EDGE);
        int ix = x + 2, iy = y + 2, iw = w - 4, ih = h - 4;
        g.fill(ix, iy, ix + iw, iy + ih, TUBE_DARK);
        if (fluid != null) {
            int fill = Math.max(1, Math.min(ih, (int) ((long) ih * amount / capacity)));
            drawFluid(g, fluid, ix, iy + ih - fill, iw, fill);
        }
        for (int t = 0; t < ih; t += 3) {
            int len = t % 15 == 0 ? 4 : 2;
            g.fill(ix, iy + t, ix + len, iy + t + 1, TUBE_TICK);
            g.fill(ix + iw - len, iy + t, ix + iw, iy + t + 1, TUBE_TICK);
        }
        String label = amount >= 1000 ? (amount / 100) / 10.0 + "B" : amount + "";
        g.drawString(font, label, x + (w - font.width(label)) / 2, y + h + 3, 0xFFFFFFFF, true);
    }

    /** Position of the icon of a pump setting in Sophisticated's icons.png: {on-state u, off-state u, v}. */
    private static int[] icon(int action) {
        return switch (action) {
            case DIRECTION -> new int[]{144, 160, 0};
            case WORLD -> new int[]{176, 192, 0};
            case STORAGES -> new int[]{0, 16, 112};
            default -> new int[]{208, 224, 0};
        };
    }

    private static boolean settingOn(Box box) {
        PumpUpgradeItem item = isFluidPump(box.stack()) ? (PumpUpgradeItem) box.stack().getItem() : null;
        return switch (box.action()) {
            case DIRECTION -> flag(box.stack(), ModCoreDataComponents.IS_INPUT, true);
            case WORLD -> flag(box.stack(), ModCoreDataComponents.INTERACT_WITH_WORLD, item != null && item.getInteractWithWorldDefault());
            case STORAGES -> flag(box.stack(), ModCoreDataComponents.INTERACT_WITH_FLUID_HANDLERS,
                    item != null && item.getInteractWithFluidHandlersDefault());
            default -> flag(box.stack(), ModCoreDataComponents.INTERACT_WITH_HAND, item != null && item.getInteractWithHandDefault());
        };
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !supported(screen)) return;
        GuiGraphics g = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Box hovered = null;
        List<Box> boxes = boxes(screen);
        g.pose().pushPose();
        // above the item lists of other mods, but below item tooltips (which are drawn at z 400)
        g.pose().translate(0, 0, 300);
        for (Box box : boxes) {
            boolean on = enabled(box.stack());
            if (box.kind() == Kind.TUBE) {
                drawTube(g, box, mc.font);
            } else if (box.kind() == Kind.BG) {
                g.fill(box.x() - 1, box.y() - 1, box.x() + box.w() + 1, box.y() + box.h() + 1, GOLD);
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), PANEL);
                g.renderItem(box.stack(), box.x() + 3, box.y() + 3);
                String name = mc.font.plainSubstrByWidth(box.stack().getHoverName().getString(), box.w() - 28);
                g.drawString(mc.font, name, box.x() + 24, box.y() + 7, 0xFFF3D9B0, false);
            } else if (box.kind() == Kind.PANEL && box.action() != ON_OFF) {
                // the square buttons of Sophisticated's own pump tab: black frame, gold body, icon
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), 0xFF000000);
                g.fill(box.x() + 1, box.y() + 1, box.x() + box.w() - 1, box.y() + box.h() - 1, 0xFFF2C25A);
                g.fill(box.x() + 1, box.y() + box.h() - 3, box.x() + box.w() - 1, box.y() + box.h() - 1, 0xFFC8963A);
                if (box.action() == STORAGES) {
                    // Sophisticated's own storages icon is empty in this pack: ours (a tank with a pipe)
                    g.blit(settingOn(box) ? STORAGE_ON : STORAGE_OFF, box.x() + 2, box.y() + 2, 0, 0, 16, 16, 16, 16);
                } else {
                    int[] icon = icon(box.action());
                    g.blit(ICONS, box.x() + 2, box.y() + 2, settingOn(box) ? icon[0] : icon[1], icon[2], 16, 16, 256, 256);
                }
            } else if (box.kind() == Kind.PANEL) {
                g.fill(box.x() - 1, box.y() - 1, box.x() + box.w() + 1, box.y() + box.h() + 1, on ? GOLD : SLOT_EDGE);
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), on ? 0xFFE8A030 : 0xFF5A3029);
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + 1, 0x33FFFFFF);
                String label = on ? "Включено" : "Выключено";
                g.drawString(mc.font, label, box.x() + (box.w() - mc.font.width(label)) / 2, box.y() + (box.h() - 8) / 2 + 1,
                        on ? 0xFF2A1708 : 0xFFD9B58C, false);
            } else {
                boolean selected = openTab == box.category() * 16 + box.socket();
                int border = on ? GOLD : SLOT_EDGE;
                g.fill(box.x() - 1, box.y() - 1, box.x() + box.w() + 1, box.y() + box.h() + 1, border);
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), selected ? 0xFF8E5A4C : SLOT);
                g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + 1, 0x22FFFFFF);
                int pad = (box.w() - 16) / 2;
                g.renderItem(box.stack(), box.x() + pad, box.y() + pad);
                if (!on) g.fill(box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), 0x99301410);
            }
            if (box.kind() != Kind.BG && box.contains(event.getMouseX(), event.getMouseY())) hovered = box;
        }
        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            if (hovered.kind() == Kind.PANEL) {
                tip.add(Component.literal(hovered.action() == ON_OFF ? (enabled(hovered.stack()) ? "Выключить" : "Включить")
                        : panelLabel(hovered)));
            } else {
                tip.add(hovered.stack().getHoverName());
                var fluid = tankFluid(hovered.stack());
                if (isTank(hovered.stack())) {
                    int amount = fluid == null ? 0 : fluid.getAmount();
                    tip.add(Component.translatable("spellclasses.implant_tank",
                                    fluid == null ? Component.translatable("spellclasses.implant_tank_empty") : fluid.getHoverName(),
                                    amount, capacity(hovered.stack(), amount))
                            .withStyle(net.minecraft.ChatFormatting.AQUA));
                } else {
                    tip.add(Component.translatable("spellclasses.implant_tab_open").withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
            g.renderComponentTooltip(mc.font, tip, event.getMouseX(), event.getMouseY());
        }
        g.pose().popPose();
    }

    private static FluidStack tankFluid(ItemStack stack) {
        var content = stack.get(ModCoreDataComponents.FLUID_CONTENTS);
        if (content == null) return null;
        var fluid = content.copy();
        return fluid.isEmpty() ? null : fluid;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !supported(screen) || event.getButton() != 0) return;
        for (Box box : boxes(screen)) {
            if (box.kind() == Kind.BG || !box.contains(event.getMouseX(), event.getMouseY())) continue;
            switch (box.kind()) {
                case TOGGLE -> {
                    int key = box.category() * 16 + box.socket();
                    openTab = openTab == key ? -1 : key;
                }
                case PANEL -> PacketDistributor.sendToServer(new ImplantUpgradePayload(box.category(), box.socket(), box.action()));
                default -> {
                    return;
                }
            }
            event.setCanceled(true);
            return;
        }
    }
}
