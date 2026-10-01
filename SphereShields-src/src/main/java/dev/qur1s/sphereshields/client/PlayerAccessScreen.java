package dev.qur1s.sphereshields.client;

import dev.qur1s.sphereshields.network.SetAllowMobsPayload;
import dev.qur1s.sphereshields.network.SetAllowedPlayersPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The shield GUI's access-control screen: lists every player currently known to the client (from
 * the multiplayer player list) with a per-player toggle. Whoever is checked here is on the
 * generator's whitelist - anyone else gets pushed out of the dome like a hostile mob. Each toggle
 * sends the whole updated set to the server immediately, rather than needing a separate save step.
 * Styled to match {@code ShieldGeneratorScreen}'s own dark panel (see {@link SphereShieldsGuiStyle}).
 */
public class PlayerAccessScreen extends Screen {
    private static final int ROW_WIDTH = 180;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_SPACING = 4;
    private static final int PANEL_PADDING = 14;
    private static final int TITLE_HEIGHT = 22;

    private final Screen parent;
    private final BlockPos generatorPos;
    private final Set<UUID> allowed;
    private boolean allowMobs;
    private final Map<UUID, ThemedButton> rowButtons = new LinkedHashMap<>();
    private ThemedButton mobsButton;
    private List<PlayerInfo> players = List.of();

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public PlayerAccessScreen(Screen parent, BlockPos generatorPos, Set<UUID> currentlyAllowed, boolean currentlyAllowMobs) {
        super(Component.literal("Доступ к куполу"));
        this.parent = parent;
        this.generatorPos = generatorPos;
        this.allowed = new LinkedHashSet<>(currentlyAllowed);
        this.allowMobs = currentlyAllowMobs;
    }

    @Override
    protected void init() {
        this.rowButtons.clear();
        this.players = new ArrayList<>();
        if (Minecraft.getInstance().getConnection() != null) {
            this.players.addAll(Minecraft.getInstance().getConnection().getOnlinePlayers());
        }
        this.players.sort(Comparator.comparing(info -> info.getProfile().getName()));

        // +1 row for the "Мобы" toggle, always shown first regardless of how many players are online.
        int rowCount = Math.max(this.players.size(), 1) + 1;
        this.panelWidth = ROW_WIDTH + PANEL_PADDING * 2;
        this.panelHeight = TITLE_HEIGHT + rowCount * (ROW_HEIGHT + ROW_SPACING) - ROW_SPACING + PANEL_PADDING * 2;
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;

        int rowX = this.panelX + PANEL_PADDING;
        int y = this.panelY + PANEL_PADDING + TITLE_HEIGHT;

        this.mobsButton = new ThemedButton(rowX, y, ROW_WIDTH, ROW_HEIGHT,
                Component.literal("Мобы"), b -> this.sphereshields$toggleMobs());
        this.mobsButton.setHighlighted(this.allowMobs);
        this.addRenderableWidget(this.mobsButton);
        y += ROW_HEIGHT + ROW_SPACING;

        for (PlayerInfo info : this.players) {
            UUID id = info.getProfile().getId();
            String name = info.getProfile().getName();
            ThemedButton button = new ThemedButton(rowX, y, ROW_WIDTH, ROW_HEIGHT,
                    Component.literal(name), b -> this.sphereshields$toggle(id));
            button.setHighlighted(this.allowed.contains(id));
            this.rowButtons.put(id, button);
            this.addRenderableWidget(button);
            y += ROW_HEIGHT + ROW_SPACING;
        }
    }

    private void sphereshields$toggleMobs() {
        this.allowMobs = !this.allowMobs;
        this.mobsButton.setHighlighted(this.allowMobs);
        PacketDistributor.sendToServer(new SetAllowMobsPayload(this.generatorPos, this.allowMobs));
    }

    private void sphereshields$toggle(UUID id) {
        boolean nowAllowed = !this.allowed.contains(id);
        if (nowAllowed) {
            this.allowed.add(id);
        } else {
            this.allowed.remove(id);
        }
        ThemedButton button = this.rowButtons.get(id);
        if (button != null) {
            button.setHighlighted(nowAllowed);
        }
        PacketDistributor.sendToServer(new SetAllowedPlayersPayload(this.generatorPos, new ArrayList<>(this.allowed)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight,
                SphereShieldsGuiStyle.PANEL_EDGE);
        graphics.fill(this.panelX + 1, this.panelY + 1, this.panelX + this.panelWidth - 1, this.panelY + this.panelHeight - 1,
                SphereShieldsGuiStyle.PANEL_COLOR);
        graphics.fill(this.panelX + 2, this.panelY + 2, this.panelX + this.panelWidth - 2, this.panelY + this.panelHeight - 2,
                SphereShieldsGuiStyle.PANEL_DARK);

        graphics.drawCenteredString(this.font, "Кому разрешён доступ в купол",
                this.panelX + this.panelWidth / 2, this.panelY + PANEL_PADDING - 4, SphereShieldsGuiStyle.TEXT_COLOR);

        if (this.players.isEmpty()) {
            graphics.drawCenteredString(this.font, "Нет игроков онлайн",
                    this.panelX + this.panelWidth / 2, this.panelY + PANEL_PADDING + TITLE_HEIGHT + 6,
                    SphereShieldsGuiStyle.MUTED_TEXT_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
