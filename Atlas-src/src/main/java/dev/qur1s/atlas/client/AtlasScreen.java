package dev.qur1s.atlas.client;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasLiveEntry;
import dev.qur1s.atlas.data.AtlasMarker;
import dev.qur1s.atlas.data.AtlasSheet;
import dev.qur1s.atlas.network.EditMarkerPayload;
import dev.qur1s.atlas.network.AddMarkerPayload;
import dev.qur1s.atlas.network.TeleportPayload;
import dev.qur1s.atlas.network.RemoveMarkerPayload;
import dev.qur1s.atlas.network.RequestLivePayload;
import dev.qur1s.atlas.network.RequestTracksPayload;
import dev.qur1s.atlas.network.RequestAtlasPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Atlas screen: the whole window is parchment with the explored map on it, a button strip on the left, the world's
 * name on a plate at the top, coordinates and scale bottom left and a compass bottom right. Two view modes: the
 * player's head kept in the centre, or a free view you drag around.
 */
public class AtlasScreen extends Screen {
    private static final ResourceLocation PARCHMENT_BG = ResourceLocation.fromNamespaceAndPath("atlas", "textures/gui/parchment_bg.png");
    private static final int BG_W = 816, BG_H = 630;
    private static final int BG_U = 22, BG_V = 56, BG_REGION_W = 772, BG_REGION_H = 506;
    private static final ResourceLocation PLATE = ResourceLocation.fromNamespaceAndPath("atlas", "plate");
    private static final ResourceLocation COMPASS = ResourceLocation.fromNamespaceAndPath("atlas", "textures/gui/compass.png");
    private static final ResourceLocation ICON_HEAD = icon("head");
    private static final ResourceLocation ICON_HAND = icon("hand");
    private static final ResourceLocation ICON_GLOBE = icon("globe");
    private static final ResourceLocation ICON_PLUS = icon("plus");
    private static final ResourceLocation ICON_MINUS = icon("minus");
    private static final ResourceLocation ICON_CLOSE = icon("close");
    private static final ResourceLocation ICON_HOUSE = icon("house");

    private static final int HEAD_SIZE = 12;
    private static final int MARKER_SIZE = 14;
    /** Parchment left showing around the map view. */
    private static final int FRAME = 0;
    private static final int BUTTON = 24;
    private static final int INK = 0xFF4B2E13;
    private static final double MIN_ZOOM = 0.1, MAX_ZOOM = 16.0;
    private static final UUID HOME_ID = new UUID(0L, 0L);

    private static final boolean SABLE_LOADED = net.neoforged.fml.ModList.get().isLoaded("sable");

    private enum Mode { CENTERED, FREE }

    private static ResourceLocation icon(String name) {
        return ResourceLocation.fromNamespaceAndPath("atlas", "textures/gui/icons/" + name + ".png");
    }

    private final UUID atlasId;
    private String dimension;
    private final String ownDimension;
    private Mode mode = Mode.FREE;
    private double viewX, viewZ;

    {
        // on opening the view starts on the player's head; after that it is free
        var player = Minecraft.getInstance().player;
        if (player != null) {
            viewX = player.getX();
            viewZ = player.getZ();
        }
    }
    private double zoom = 2.0;
    private AtlasButton modeButton, playersButton, autoButton, fullButton, railsButton;
    private static boolean showRails = true;
    private static final boolean CREATE_LOADED = net.neoforged.fml.ModList.get().isLoaded("create");
    private static boolean hideAuto;
    private boolean playersOpen;
    private final List<AbstractWidget> playerChoices = new ArrayList<>();
    private List<dev.qur1s.atlas.data.AtlasPlayerEntry> shownPlayers = List.of();
    private final List<AtlasButton> dimensionChoices = new ArrayList<>();
    private boolean dropdownOpen;
    private final List<AbstractWidget> contextMenu = new ArrayList<>();
    private boolean recentre;
    private List<String> shownChoices = List.of();
    private AtlasMarker hoveredMarker;
    private AtlasLiveEntry hoveredLive;
    private int liveTimer;

    // ---- marker edit panel ----
    private static final int PANEL_W = 156;
    private AtlasMarker editing;
    private String editIcon = AtlasMarker.DEFAULT_ICON;
    private boolean iconListOpen;
    private final List<AbstractWidget> panelWidgets = new ArrayList<>();
    private final List<AbstractWidget> iconListWidgets = new ArrayList<>();
    private EditBox nameBox, xBox, yBox, zBox, gotoX, gotoZ;
    private MultiLineEditBox descriptionBox;
    private AtlasButton iconButton;

    public AtlasScreen(UUID atlasId) {
        super(Component.translatable("screen.atlas.title"));
        this.atlasId = atlasId;
        this.ownDimension = Minecraft.getInstance().level == null ? "minecraft:overworld"
                : Minecraft.getInstance().level.dimension().location().toString();
        this.dimension = ownDimension;
    }

    // ---------- layout ----------

    private int titleW() {
        return this.font.width(Component.translatable("screen.atlas.plate", dimensionName(dimension))) + 32;
    }

    private int titleX() {
        return this.width / 2 - titleW() / 2;
    }

    private int toolbarX() {
        return FRAME + 8;
    }

    private int toolbarY() {
        return FRAME + 8;
    }

    @Override
    protected void init() {
        ClientAtlasCache.reset(atlasId, dimension);
        if (atlasId != null) {
            PacketDistributor.sendToServer(new RequestAtlasPayload(atlasId, dimension));
            PacketDistributor.sendToServer(new RequestTracksPayload(atlasId, dimension, true));
        }
        int x = toolbarX() + 4, y = toolbarY() + 4, step = BUTTON + 4;
        modeButton = this.addRenderableWidget(new AtlasButton(x, y, BUTTON, BUTTON, modeLabel(),
                () -> mode == Mode.CENTERED ? ICON_HEAD : ICON_HAND, button -> toggleMode()));
        this.addRenderableWidget(new AtlasButton(x, y + step, BUTTON, BUTTON,
                Component.translatable("screen.atlas.zoom_in"), () -> ICON_PLUS, button -> zoomBy(1.25)));
        this.addRenderableWidget(new AtlasButton(x, y + 2 * step, BUTTON, BUTTON,
                Component.translatable("screen.atlas.zoom_out"), () -> ICON_MINUS, button -> zoomBy(0.8)));
        this.addRenderableWidget(new AtlasButton(x, y + 3 * step, BUTTON, BUTTON,
                Component.translatable("screen.atlas.close"), () -> ICON_CLOSE, button -> this.onClose()));
        addGotoBoxes();
        playersButton = this.addRenderableWidget(new AtlasButton(playersX() + 4, toolbarY() + 4, BUTTON, BUTTON,
                Component.translatable("screen.atlas.players"), () -> ICON_HEAD, button -> togglePlayers()));
        railsButton = this.addRenderableWidget(new AtlasButton(playersX() + 4, toolbarY() + 4 + 2 * (BUTTON + 4), BUTTON, BUTTON,
                railsLabel(), () -> icon(showRails ? "rails" : "rails_off"), button -> {
                    showRails = !showRails;
                    railsButton.setLabel(railsLabel());
                    if (showRails && atlasId != null) PacketDistributor.sendToServer(new RequestTracksPayload(atlasId, dimension, true));
                }));
        railsButton.visible = CREATE_LOADED;
        fullButton = this.addRenderableWidget(new AtlasButton(playersX() + 4, toolbarY() + 4 + (CREATE_LOADED ? 3 : 2) * (BUTTON + 4), BUTTON, BUTTON,
                Component.translatable("screen.atlas.load_whole"), () -> ICON_GLOBE, button -> {
                    if (atlasId != null) PacketDistributor.sendToServer(new dev.qur1s.atlas.network.LoadWholeMapPayload(atlasId, dimension));
                }));
        fullButton.visible = false;
        autoButton = this.addRenderableWidget(new AtlasButton(playersX() + 4, toolbarY() + 4 + BUTTON + 4, BUTTON, BUTTON,
                autoLabel(), () -> icon(hideAuto ? "eye_off" : "eye"), button -> {
                    hideAuto = !hideAuto;
                    autoButton.setLabel(autoLabel());
                }));
    }

    // ---------- player search ----------

    private Component railsLabel() {
        return Component.translatable(showRails ? "screen.atlas.rails_hide" : "screen.atlas.rails_show");
    }

    private static boolean isCreative() {
        var player = Minecraft.getInstance().player;
        return player != null && player.getAbilities().instabuild;
    }

    private Component autoLabel() {
        return Component.translatable(hideAuto ? "screen.atlas.show_auto" : "screen.atlas.hide_auto");
    }

    private int playersX() {
        return this.width - toolbarX() - (BUTTON + 8);
    }

    private void togglePlayers() {
        playersOpen = !playersOpen;
        if (playersOpen && dropdownOpen) toggleDropdown();
        if (playersOpen && atlasId != null) PacketDistributor.sendToServer(new RequestLivePayload(atlasId, dimension));
        rebuildPlayers();
    }

    private void rebuildPlayers() {
        playerChoices.forEach(this::removeWidget);
        playerChoices.clear();
        shownPlayers = List.of();
        if (!playersOpen) return;
        shownPlayers = ClientAtlasCache.players();
        int w = 150, x = playersX() - 4 - w, y = toolbarY();
        for (var entry : shownPlayers) {
            AtlasButton row = new AtlasButton(x, y, w, 20, Component.literal("      " + entry.name()), null, button -> gotoPlayer(entry));
            playerChoices.add(this.addRenderableWidget(row));
            y += 22;
        }
    }

    private void gotoPlayer(dev.qur1s.atlas.data.AtlasPlayerEntry entry) {
        playersOpen = false;
        rebuildPlayers();
        if (!entry.dimension().equals(dimension)) selectDimension(entry.dimension());
        mode = Mode.FREE;
        modeButton.setLabel(modeLabel());
        recentre = false;
        viewX = entry.x() + 0.5;
        viewZ = entry.z() + 0.5;
    }

    private void drawPlayerHeads(GuiGraphics graphics) {
        var connection = Minecraft.getInstance().getConnection();
        for (int i = 0; i < playerChoices.size() && i < shownPlayers.size(); i++) {
            AbstractWidget row = playerChoices.get(i);
            var info = connection == null ? null : connection.getPlayerInfo(shownPlayers.get(i).id());
            PlayerSkin skin = info != null ? info.getSkin() : net.minecraft.client.resources.DefaultPlayerSkin.get(shownPlayers.get(i).id());
            PlayerFaceRenderer.draw(graphics, skin, row.getX() + 6, row.getY() + 4, 12);
        }
    }

    private void addGotoBoxes() {
        int bx = FRAME + 8, by = this.height - FRAME - 8 - 18;
        gotoX = new InkField(this.font, bx + 16, by + 4, 44, 12, Component.literal("X"));
        gotoZ = new InkField(this.font, bx + 76, by + 4, 44, 12, Component.literal("Z"));
        for (EditBox box : new EditBox[]{gotoX, gotoZ}) {
            box.setMaxLength(9);
            box.setBordered(false);
            box.setTextColor(INK);
            box.setFilter(text -> text.matches("-?\\d*"));
            this.addWidget(box);
        }
    }

    private static final float SMALL = 0.75f;
    /** Station and train names show up only when zoomed in this far (pixels per block), so they do not pile up. */
    private static final double STATION_LABEL_ZOOM = 1.0;

    private void smallText(GuiGraphics graphics, Component text, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(SMALL, SMALL, 1f);
        graphics.drawString(this.font, text, 0, 0, INK, false);
        graphics.pose().popPose();
    }

    private void renderSmall(GuiGraphics graphics, EditBox box, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(box.getX(), box.getY(), 0);
        graphics.pose().scale(SMALL, SMALL, 1f);
        graphics.pose().translate(-box.getX(), -box.getY(), 0);
        box.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }

    private void applyGoto() {
        if (gotoX == null || gotoX.getValue().isEmpty() || gotoZ.getValue().isEmpty()) return;
        try {
            viewX = Integer.parseInt(gotoX.getValue()) + 0.5;
            viewZ = Integer.parseInt(gotoZ.getValue()) + 0.5;
            mode = Mode.FREE;
            modeButton.setLabel(modeLabel());
            recentre = false;
        } catch (NumberFormatException ignored) {
        }
    }

    // ---------- modes and zoom ----------

    private Component modeLabel() {
        return Component.translatable(mode == Mode.CENTERED ? "screen.atlas.mode.centered" : "screen.atlas.mode.free");
    }

    private void toggleMode() {
        mode = mode == Mode.CENTERED ? Mode.FREE : Mode.CENTERED;
        modeButton.setLabel(modeLabel());
    }

    private void zoomBy(double factor) {
        zoom = Mth.clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
    }

    // ---------- dimensions ----------

    private static Component dimensionName(String id) {
        return switch (id) {
            case "minecraft:overworld" -> Component.translatable("screen.atlas.dimension.overworld");
            case "minecraft:the_nether" -> Component.translatable("screen.atlas.dimension.nether");
            case "minecraft:the_end" -> Component.translatable("screen.atlas.dimension.end");
            default -> {
                String path = id.substring(id.indexOf(':') + 1).replace('_', ' ');
                yield Component.literal(path.isEmpty() ? id : Character.toUpperCase(path.charAt(0)) + path.substring(1));
            }
        };
    }

    private Component dimensionLabel() {
        return Component.translatable("screen.atlas.dimension", dimensionName(dimension));
    }

    /** The dimensions to offer: everything the atlas has something in, plus the one the player is standing in. */
    private List<String> choices() {
        List<String> list = new ArrayList<>(ClientAtlasCache.dimensions());
        if (!list.contains(ownDimension)) list.add(0, ownDimension);
        return list;
    }

    private void toggleDropdown() {
        dropdownOpen = !dropdownOpen;
        rebuildChoices();
    }

    private void rebuildChoices() {
        dimensionChoices.forEach(this::removeWidget);
        dimensionChoices.clear();
        shownChoices = List.of();
        if (!dropdownOpen) return;
        shownChoices = choices();
        int x = titleX() + titleW() / 2 - 70, y = FRAME + 4 + 18 + 3;
        for (String id : shownChoices) {
            AtlasButton choice = new AtlasButton(x, y, 140, 20, dimensionName(id), null, button -> selectDimension(id));
            dimensionChoices.add(this.addRenderableWidget(choice));
            y += 22;
        }
    }

    private void selectDimension(String id) {
        dropdownOpen = false;
        rebuildChoices();
        if (id.equals(dimension)) return;
        dimension = id;
        ClientAtlasCache.reset(atlasId, dimension);
        if (atlasId != null) {
            PacketDistributor.sendToServer(new RequestAtlasPayload(atlasId, dimension));
            PacketDistributor.sendToServer(new RequestTracksPayload(atlasId, dimension, true));
        }
        mode = Mode.FREE;
        if (id.equals(ownDimension)) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                viewX = player.getX();
                viewZ = player.getZ();
            }
        } else {
            // the head does not exist in another dimension, so the view starts on the mapped area
            recentre = true;
        }
        modeButton.setLabel(modeLabel());
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingPanel != null) {
            AtlasMarker marker = pendingPanel;
            pendingPanel = null;
            openPanel(marker);
        }
        if (atlasId != null && liveTimer++ % 20 == 0) {
            PacketDistributor.sendToServer(new RequestLivePayload(atlasId, dimension));
        }
        if (atlasId != null && liveTimer % 200 == 100) {
            PacketDistributor.sendToServer(new RequestTracksPayload(atlasId, dimension, true));
        }
    }

    @Override
    public void removed() {
        ClientAtlasCache.clear();
        if (SABLE_LOADED) SableShips.clear();
        super.removed();
    }

    // ---------- drawing ----------

    private static void plate(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.blitSprite(PLATE, x, y, w, h);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // plain parchment over the whole window, no frame
        // the sheet with its border fills the whole window
        // a slightly zoomed-in part of the sheet: its border runs along the edges of the window
        graphics.blit(PARCHMENT_BG, 0, 0, this.width, this.height, BG_U, BG_V, BG_REGION_W, BG_REGION_H, BG_W, BG_H);

        Minecraft mc = Minecraft.getInstance();
        if (recentre) {
            if (!ClientAtlasCache.sheets().isEmpty()) {
                double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
                for (var sheet : ClientAtlasCache.sheets()) {
                    double half = sheet.blocks() / 2.0;
                    minX = Math.min(minX, sheet.centreX() - half);
                    maxX = Math.max(maxX, sheet.centreX() + half);
                    minZ = Math.min(minZ, sheet.centreZ() - half);
                    maxZ = Math.max(maxZ, sheet.centreZ() + half);
                }
                viewX = (minX + maxX) / 2.0;
                viewZ = (minZ + maxZ) / 2.0;
                recentre = false;
            } else if (!ClientAtlasCache.markers().isEmpty()) {
                viewX = ClientAtlasCache.markers().get(0).x();
                viewZ = ClientAtlasCache.markers().get(0).z();
                recentre = false;
            }
        }
        // the list of dimensions arrives after the screen opens; refresh an open dropdown when it does
        if (dropdownOpen && !choices().equals(shownChoices)) rebuildChoices();
        if (mode == Mode.CENTERED && mc.player != null) {
            viewX = mc.player.getX();
            viewZ = mc.player.getZ();
        }

        int vx = FRAME, vy = FRAME, vw = this.width - 2 * FRAME, vh = this.height - 2 * FRAME;
        double centreX = vx + vw / 2.0, centreY = vy + vh / 2.0;
        graphics.enableScissor(vx, vy, vx + vw, vy + vh);
        // the map sheets, coarse ones first so that the detailed ones lie on top of them
        for (var sheet : ClientAtlasCache.sheets()) {
            double half = sheet.blocks() / 2.0;
            double left = centreX + (sheet.centreX() - half - viewX) * zoom, top = centreY + (sheet.centreZ() - half - viewZ) * zoom;
            double size = sheet.blocks() * zoom;
            if (left > vx + vw || top > vy + vh || left + size < vx || top + size < vy) continue;
            int sx = Mth.floor(left), sy = Mth.floor(top);
            graphics.blit(sheet.location(), sx, sy, Mth.ceil(left + size) - sx, Mth.ceil(top + size) - sy,
                    0, 0, AtlasSheet.SIZE, AtlasSheet.SIZE, AtlasSheet.SIZE, AtlasSheet.SIZE);
        }
        drawStitches(graphics, centreX, centreY, vx, vy, vw, vh);

        drawTracks(graphics, centreX, centreY, vx, vy, vw, vh);

        if (SABLE_LOADED && dimension.equals(ownDimension)) {
            SableShips.draw(graphics, this.font, viewX, viewZ, zoom, centreX, centreY);
        }

        AtlasLiveEntry hoveredLive = null;
        for (AtlasLiveEntry entry : ClientAtlasCache.live()) {
            int ex = Mth.floor(centreX + (entry.x() + 0.5 - viewX) * zoom) - MARKER_SIZE / 2;
            int ey = Mth.floor(centreY + (entry.z() + 0.5 - viewZ) * zoom) - MARKER_SIZE / 2;
            if (ex + MARKER_SIZE < vx || ey + MARKER_SIZE < vy || ex > vx + vw || ey > vy + vh) continue;
            if (entry.kind() == AtlasLiveEntry.GATE) {
                graphics.blit(icon("gate"), ex, ey, MARKER_SIZE, MARKER_SIZE, 0, 0, 16, 16, 16, 16);
            } else if (entry.kind() == AtlasLiveEntry.TRAIN || entry.kind() == AtlasLiveEntry.STATION) {
                graphics.blit(icon(entry.kind() == AtlasLiveEntry.TRAIN ? "train" : "station"), ex, ey, MARKER_SIZE, MARKER_SIZE, 0, 0, 16, 16, 16, 16);
                if (!entry.name().isEmpty() && zoom >= STATION_LABEL_ZOOM) {
                    AtlasLabels.draw(graphics, this.font, entry.name(), ex + MARKER_SIZE / 2, ey + MARKER_SIZE + 1);
                }
            } else {
                var connection = Minecraft.getInstance().getConnection();
                var info = connection == null ? null : connection.getPlayerInfo(entry.id());
                PlayerSkin skin = info != null ? info.getSkin() : net.minecraft.client.resources.DefaultPlayerSkin.get(entry.id());
                PlayerFaceRenderer.draw(graphics, skin, ex - 1, ey - 1, HEAD_SIZE);
            }
            if (!overPanel(mouseX) && mouseX >= ex && mouseX < ex + MARKER_SIZE && mouseY >= ey && mouseY < ey + MARKER_SIZE) {
                hoveredLive = entry;
            }
        }
        this.hoveredLive = hoveredLive;

        AtlasMarker hovered = null;
        for (AtlasMarker marker : ClientAtlasCache.markers()) {
            if (hideAuto && marker.color() == AtlasMarker.COLOR_STRUCTURE) continue;
            int mx = Mth.floor(centreX + (marker.x() + 0.5 - viewX) * zoom) - MARKER_SIZE / 2;
            int my = Mth.floor(centreY + (marker.z() + 0.5 - viewZ) * zoom) - MARKER_SIZE / 2;
            if (mx + MARKER_SIZE < vx || my + MARKER_SIZE < vy || mx > vx + vw || my > vy + vh) continue;
            int texSize = isVanillaPicture(marker) ? 8 : 16;
            graphics.blit(markerTexture(marker), mx, my, MARKER_SIZE, MARKER_SIZE, 0, 0, texSize, texSize, texSize, texSize);
            if (!overPanel(mouseX) && mouseX >= mx && mouseX < mx + MARKER_SIZE && mouseY >= my && mouseY < my + MARKER_SIZE) {
                hovered = marker;
            }
        }
        hoveredMarker = hovered;

        if (mc.player != null && dimension.equals(ownDimension)) {
            PlayerSkin skin = mc.player.getSkin();
            int hx = Mth.floor(centreX + (mc.player.getX() - viewX) * zoom) - HEAD_SIZE / 2;
            int hy = Mth.floor(centreY + (mc.player.getZ() - viewZ) * zoom) - HEAD_SIZE / 2;
            PlayerFaceRenderer.draw(graphics, skin, hx, hy, HEAD_SIZE);
        }
        graphics.disableScissor();

        drawFurniture(graphics, mouseX, mouseY, centreX, centreY);
        if (editing != null) drawPanel(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (gotoX != null) {
            renderSmall(graphics, gotoX, mouseX, mouseY, partialTick);
            renderSmall(graphics, gotoZ, mouseX, mouseY, partialTick);
        }
        drawPanelIcons(graphics);
        if (playersButton != null) {
            playersButton.visible = editing == null;
            autoButton.visible = editing == null;
            fullButton.visible = editing == null && isCreative();
            railsButton.visible = editing == null && CREATE_LOADED;
            if (editing != null && playersOpen) togglePlayers();
            if (playersOpen && !ClientAtlasCache.players().equals(shownPlayers)) rebuildPlayers();
            drawPlayerHeads(graphics);
        }

        if (hoveredLive != null && hoveredMarker == null) {
            List<Component> lines = new ArrayList<>();
            if (hoveredLive.kind() == AtlasLiveEntry.GATE) {
                lines.add(Component.translatable("screen.atlas.live.gate"));
            } else if (hoveredLive.kind() == AtlasLiveEntry.TRAIN || hoveredLive.kind() == AtlasLiveEntry.STATION) {
                lines.add(Component.literal(hoveredLive.name()));
                lines.add(Component.translatable(hoveredLive.kind() == AtlasLiveEntry.TRAIN ? "screen.atlas.live.train" : "screen.atlas.live.station").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                lines.add(Component.literal(hoveredLive.name()));
                lines.add(Component.translatable("screen.atlas.live.player").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
        if (hoveredMarker != null) {
            boolean home = hoveredMarker.id().equals(HOME_ID);
            String name = home ? Component.translatable("screen.atlas.marker.home").getString() : displayName(hoveredMarker);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(name));
            if (!hoveredMarker.description().isEmpty()) {
                String text = hoveredMarker.description().length() > 120
                        ? hoveredMarker.description().substring(0, 120) + "…" : hoveredMarker.description();
                lines.add(Component.literal(text.replace('\n', ' ')).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }


    // ---------- marker edit panel ----------

    private int panelX() {
        return this.width - PANEL_W - 8;
    }

    private boolean overPanel(double mouseX) {
        return editing != null && mouseX >= panelX();
    }

    private void openPanel(AtlasMarker marker) {
        closePanel();
        editing = marker;
        editIcon = marker.icon();
        int px = panelX(), top = 8, w = PANEL_W - 16, left = px + 8;
        int ph = this.height - 16;
        panelWidgets.add(this.addRenderableWidget(new AtlasButton(left, top + 8, 20, 20,
                Component.translatable("screen.atlas.edit.delete"), () -> icon("trash"), b -> deleteEdited())));
        panelWidgets.add(this.addRenderableWidget(new AtlasButton(px + PANEL_W - 28, top + 8, 20, 20,
                Component.translatable("screen.atlas.edit.cancel"), () -> ICON_CLOSE, b -> closePanel())));

        fieldBackgrounds.clear();
        nameBox = inkBox(left, top + 54, w, 20, Component.translatable("screen.atlas.edit.name"));
        nameBox.setMaxLength(AtlasMarker.MAX_NAME);
        nameBox.setValue(marker.color() == AtlasMarker.COLOR_STRUCTURE || (marker.color() == AtlasMarker.COLOR_DEATH && marker.name().isEmpty()) ? displayName(marker) : marker.name());
        panelWidgets.add(this.addRenderableWidget(nameBox));

        iconButton = new AtlasButton(left, top + 96, w, 22, iconLabel(), null, b -> toggleIconList());
        panelWidgets.add(this.addRenderableWidget(iconButton));

        int third = (w - 8) / 3;
        xBox = coordinateBox(left, top + 146, third, marker.x(), "X");
        yBox = coordinateBox(left + third + 4, top + 146, third, marker.y(), "Y");
        zBox = coordinateBox(left + 2 * (third + 4), top + 146, third, marker.z(), "Z");
        panelWidgets.add(this.addRenderableWidget(new AtlasButton(left + w - 20, top + 124, 20, 18,
                Component.translatable("screen.atlas.edit.here"), () -> icon("target"), b -> setHere())));

        var self = Minecraft.getInstance().player;
        boolean canTeleport = self != null && self.hasPermissions(2);
        boolean canTrack = self != null && !dev.qur1s.atlas.AtlasCompass.inHands(self).isEmpty();
        int descTop = top + 190;
        descriptionBox = new ParchmentTextArea(this.font, left, descTop, w,
                Math.max(60, ph - (descTop - top) - 54 - (canTeleport ? 28 : 0) - (canTrack ? 28 : 0)),
                Component.empty(), Component.translatable("screen.atlas.edit.description"));
        descriptionBox.setCharacterLimit(AtlasMarker.MAX_DESCRIPTION);
        descriptionBox.setValue(marker.description());
        panelWidgets.add(this.addRenderableWidget(descriptionBox));

        if (canTrack) {
            panelWidgets.add(this.addRenderableWidget(new AtlasButton(left, top + ph - (canTeleport ? 88 : 60), w, 22,
                    Component.translatable("screen.atlas.track"), null, b -> trackEdited())));
        }
        if (canTeleport) {
            panelWidgets.add(this.addRenderableWidget(new AtlasButton(left, top + ph - 60, w, 22,
                    Component.translatable("screen.atlas.menu.teleport"), null, b -> teleportToEdited())));
        }
        int half = (w - 4) / 2;
        panelWidgets.add(this.addRenderableWidget(new AtlasButton(left, top + ph - 32, half, 24,
                Component.translatable("screen.atlas.edit.save"), null, b -> saveEdited())));
        panelWidgets.add(this.addRenderableWidget(new AtlasButton(left + half + 4, top + ph - 32, half, 24,
                Component.translatable("screen.atlas.edit.cancel"), null, b -> closePanel())));
    }

    private AtlasMarker pendingPanel;
    private final List<int[]> fieldBackgrounds = new ArrayList<>();

    /** A single-line field in ink on parchment; the background rectangle is drawn with the panel. */
    private EditBox inkBox(int x, int y, int width, int height, Component label) {
        fieldBackgrounds.add(new int[]{x, y, width, height});
        EditBox box = new InkField(this.font, x + 5, y + (height - 8) / 2, width - 10, 10, label);
        box.setBordered(false);
        box.setTextColor(0xFF3B2410);
        box.setTextColorUneditable(0xFF3B2410);
        return box;
    }

    private void trackEdited() {
        if (atlasId == null || xBox == null) return;
        PacketDistributor.sendToServer(new dev.qur1s.atlas.network.TrackMarkerPayload(atlasId, dimension,
                parse(xBox, editing.x()), parse(yBox, editing.y()), parse(zBox, editing.z())));
        this.onClose();
    }

    private void teleportToEdited() {
        if (atlasId == null || xBox == null) return;
        int x = parse(xBox, editing.x()), z = parse(zBox, editing.z());
        PacketDistributor.sendToServer(new TeleportPayload(atlasId, dimension, x, z));
        this.onClose();
    }

    private EditBox coordinateBox(int x, int y, int width, int value, String name) {
        EditBox box = inkBox(x, y, width, 20, Component.literal(name));
        box.setMaxLength(9);
        box.setFilter(text -> text.matches("-?\\d*"));
        box.setValue(Integer.toString(value));
        panelWidgets.add(this.addRenderableWidget(box));
        return box;
    }

    private Component iconLabel() {
        return Component.translatable("screen.atlas.edit.icon", Component.translatable("screen.atlas.icon." + editIcon));
    }

    private void toggleIconList() {
        iconListOpen = !iconListOpen;
        iconListWidgets.forEach(this::removeWidget);
        iconListWidgets.clear();
        // the fields under the list are hidden while it is open so they cannot take its clicks
        for (AbstractWidget w : new AbstractWidget[]{xBox, yBox, zBox, descriptionBox}) {
            if (w != null) w.visible = !iconListOpen;
        }
        if (!iconListOpen) return;
        int left = iconButton.getX(), y = iconButton.getY() + iconButton.getHeight() + 2;
        int listHeight = AtlasMarker.ICONS.size() * 21;
        if (y + listHeight > this.height - 6) y = Math.max(6, this.height - 6 - listHeight);
        for (String key : AtlasMarker.ICONS) {
            AtlasButton choice = new AtlasButton(left, y, iconButton.getWidth(), 20,
                    Component.translatable("screen.atlas.icon." + key), null, b -> chooseIcon(key));
            iconListWidgets.add(this.addRenderableWidget(choice));
            y += 21;
        }
    }

    private void chooseIcon(String key) {
        editIcon = key;
        iconButton.setLabel(iconLabel());
        iconListOpen = true;
        toggleIconList();        // closes the list and shows the fields again
    }

    private void setHere() {
        var player = Minecraft.getInstance().player;
        if (player == null || xBox == null) return;
        xBox.setValue(Integer.toString(Mth.floor(player.getX())));
        yBox.setValue(Integer.toString(Mth.floor(player.getY())));
        zBox.setValue(Integer.toString(Mth.floor(player.getZ())));
    }

    private static int parse(EditBox box, int fallback) {
        try {
            return Integer.parseInt(box.getValue().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void saveEdited() {
        if (editing == null || atlasId == null) return;
        AtlasMarker edited = new AtlasMarker(editing.id(), parse(xBox, editing.x()), parse(yBox, editing.y()),
                parse(zBox, editing.z()), editing.color(), nameBox.getValue(), editIcon, descriptionBox.getValue()).sanitized();
        PacketDistributor.sendToServer(new EditMarkerPayload(atlasId, dimension, edited));
        // show the edit at once; the server's list replaces it when it arrives
        List<AtlasMarker> list = ClientAtlasCache.markers();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(edited.id())) list.set(i, edited);
        }
        closePanel();
    }

    private void deleteEdited() {
        if (editing == null || atlasId == null) return;
        AtlasMarker target = editing;
        PacketDistributor.sendToServer(new RemoveMarkerPayload(atlasId, dimension, target.id()));
        ClientAtlasCache.markers().removeIf(m -> m.id().equals(target.id()));
        closePanel();
    }

    // ---------- stitches between sheets ----------

    private static final int THREAD = 0xFF9A6B3A, THREAD_EDGE = 0xFF3B2512, THREAD_TWIST = 0xFFC9A064;

    /** A bit of thread: its two ends, how thick it is (half width) and its colour. */
    private record Thread(float x1, float y1, float x2, float y2, float half, int colour) {
    }

    /**
     * Sheets that touch are sewn together along the edge of what is drawn on them: a detailed sheet lying on a coarser
     * one is sewn on where its picture ends, and equal sheets side by side are sewn along their seam.
     */
    private void drawStitches(GuiGraphics graphics, double centreX, double centreY, int vx, int vy, int vw, int vh) {
        var sheets = ClientAtlasCache.sheets();
        java.util.List<Thread> outlines = new java.util.ArrayList<>(), threads = new java.util.ArrayList<>();
        for (var a : sheets) {
            // a stitch is about 4 map pixels long, so they scale with the map
            double pixelPx = (1 << a.scale()) * zoom;
            if (pixelPx < 0.7) continue;
            int blocks = a.blocks();
            double left = a.centreX() - blocks / 2.0, top = a.centreZ() - blocks / 2.0;
            double sx = centreX + (left - viewX) * zoom, sy = centreY + (top - viewZ) * zoom, size = blocks * zoom;
            if (sx > vx + vw || sy > vy + vh || sx + size < vx || sy + size < vy) continue;
            float[] anchors = a.stitches();
            for (int i = 0; i + 3 < anchors.length; i += 4) {
                float x = (float) (centreX + (anchors[i] - viewX) * zoom), y = (float) (centreY + (anchors[i + 1] - viewZ) * zoom);
                if (x < vx - 8 || x > vx + vw + 8 || y < vy - 8 || y > vy + vh + 8) continue;
                boolean horizontal = anchors[i + 3] != 0;      // out is up or down: the seam runs sideways
                addStitch(outlines, threads, x, y, horizontal, (float) pixelPx, i / 4);
            }
        }
        if (threads.isEmpty()) return;
        var matrix = graphics.pose().last().pose();
        var buffer = graphics.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());
        for (var list : java.util.List.of(outlines, threads)) {       // the dark edges first, the thread over them
            for (Thread t : list) {
                float dx = t.x2() - t.x1(), dy = t.y2() - t.y1(), len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len < 0.01f) continue;
                float nx = -dy / len * t.half(), ny = dx / len * t.half();
                for (int w = 0; w < 2; w++) {      // both windings, the GUI type culls back faces
                    float sign = w == 0 ? 1f : -1f;
                    buffer.addVertex(matrix, t.x1() + nx * sign, t.y1() + ny * sign, 0).setColor(t.colour());
                    buffer.addVertex(matrix, t.x1() - nx * sign, t.y1() - ny * sign, 0).setColor(t.colour());
                    buffer.addVertex(matrix, t.x2() - nx * sign, t.y2() - ny * sign, 0).setColor(t.colour());
                    buffer.addVertex(matrix, t.x2() + nx * sign, t.y2() + ny * sign, 0).setColor(t.colour());
                }
            }
        }
        graphics.flush();
    }

    private static void line(java.util.List<Thread> outlines, java.util.List<Thread> threads, float x1, float y1, float x2, float y2,
                             float outlineHalf, float half, int colour) {
        outlines.add(new Thread(x1, y1, x2, y2, outlineHalf, THREAD_EDGE));
        threads.add(new Thread(x1, y1, x2, y2, half, colour));
    }

    /**
     * One stitch, like a surgeon's: a twisted thread across the seam, square to it and just a little bowed, with the
     * ends of the thread showing where it went through, and every third one tied with a knot and two tails.
     */
    private static void addStitch(java.util.List<Thread> outlines, java.util.List<Thread> threads, float x, float y,
                                  boolean horizontal, float p, int index) {
        // "along" runs with the seam, "across" runs over it
        float ax = horizontal ? 1f : 0f, ay = horizontal ? 0f : 1f;
        float cx = horizontal ? 0f : 1f, cy = horizontal ? 1f : 0f;
        float reach = 2.2f * p;
        float outlineHalf = Math.max(0.9f, 0.34f * p), half = Math.max(0.45f, 0.2f * p);
        float bow = 0.3f * p;
        int steps = p >= 3f ? 8 : 3;
        float px = 0, py = 0;
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps, u = (t - 0.5f) * 2f * reach;
            float curve = bow * (1f - (2f * t - 1f) * (2f * t - 1f));
            float qx = x + cx * u + ax * curve, qy = y + cy * u + ay * curve;
            if (i > 0) {
                // twisted rope: the light and the dark strands take turns
                int colour = p >= 3f && i % 2 == 0 ? THREAD_TWIST : THREAD;
                line(outlines, threads, px, py, qx, qy, outlineHalf, half, colour);
            }
            px = qx;
            py = qy;
        }
        if (p < 2f) return;
        // where the thread came out: a couple of little strokes along the seam at both ends
        for (int end = -1; end <= 1; end += 2) {
            float ex = x + cx * reach * end, ey = y + cy * reach * end;
            for (int k = -1; k <= 1; k += 2) {
                float sx = ex + ax * 0.4f * p * k, sy = ey + ay * 0.4f * p * k;
                line(outlines, threads, sx - ax * 0.45f * p, sy - ay * 0.45f * p, sx + ax * 0.45f * p, sy + ay * 0.45f * p,
                        0.25f, 0.25f, THREAD_EDGE);
            }
        }
        if (index % 3 == 0) {
            // a knot on one end, with two tails
            float kx = x + cx * reach, ky = y + cy * reach, r = 0.55f * p;
            line(outlines, threads, kx - ax * r, ky - ay * r, kx + cx * r, ky + cy * r, outlineHalf, half, THREAD);
            line(outlines, threads, kx + cx * r, ky + cy * r, kx + ax * r, ky + ay * r, outlineHalf, half, THREAD);
            line(outlines, threads, kx + ax * r, ky + ay * r, kx - cx * r, ky - cy * r, outlineHalf, half, THREAD);
            line(outlines, threads, kx - cx * r, ky - cy * r, kx - ax * r, ky - ay * r, outlineHalf, half, THREAD);
            line(outlines, threads, kx, ky, kx + cx * 1.3f * p + ax * 0.8f * p, ky + cy * 1.3f * p + ay * 0.8f * p, outlineHalf, half, THREAD);
            line(outlines, threads, kx, ky, kx + cx * 1.1f * p - ax * 0.9f * p, ky + cy * 1.1f * p - ay * 0.9f * p, outlineHalf, half, THREAD);
        }
    }

    // ---------- Create tracks ----------

    private static final int RAIL_DARK = 0xFF000000, RAIL_LIGHT = 0xFFF2639F;

    /** Draws the tracks as a pink line with a black outline. */
    private void drawTracks(GuiGraphics graphics, double centreX, double centreY, int vx, int vy, int vw, int vh) {
        if (!showRails) return;
        int[] segs = ClientAtlasCache.tracks();
        if (segs.length == 0) return;
        var matrix = graphics.pose().last().pose();
        float width = (float) Mth.clamp(zoom * 1.6, 3.0, 5.0);
        for (int pass = 0; pass < 2; pass++) {
            float half = pass == 0 ? width / 2f : Math.max(0.6f, width / 4f);
            int colour = pass == 0 ? RAIL_DARK : RAIL_LIGHT;
            var buffer = graphics.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());
            for (int i = 0; i + 3 < segs.length; i += 4) {
                float x1 = (float) (centreX + (segs[i] / 4.0 - viewX) * zoom), y1 = (float) (centreY + (segs[i + 1] / 4.0 - viewZ) * zoom);
                float x2 = (float) (centreX + (segs[i + 2] / 4.0 - viewX) * zoom), y2 = (float) (centreY + (segs[i + 3] / 4.0 - viewZ) * zoom);
                if (Math.max(x1, x2) < vx || Math.min(x1, x2) > vx + vw || Math.max(y1, y2) < vy || Math.min(y1, y2) > vy + vh) continue;
                float dx = x2 - x1, dy = y2 - y1, len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len < 0.01f) continue;
                float nx = -dy / len * half, ny = dx / len * half;
                // a little past both ends, so that pieces meeting at an angle leave no gap
                float ex = dx / len * half, ey = dy / len * half;
                x1 -= ex; y1 -= ey; x2 += ex; y2 += ey;
                // both windings, so the quad is seen whichever way the segment runs (the GUI type culls back faces)
                buffer.addVertex(matrix, x1 + nx, y1 + ny, 0).setColor(colour);
                buffer.addVertex(matrix, x1 - nx, y1 - ny, 0).setColor(colour);
                buffer.addVertex(matrix, x2 - nx, y2 - ny, 0).setColor(colour);
                buffer.addVertex(matrix, x2 + nx, y2 + ny, 0).setColor(colour);
                buffer.addVertex(matrix, x2 + nx, y2 + ny, 0).setColor(colour);
                buffer.addVertex(matrix, x2 - nx, y2 - ny, 0).setColor(colour);
                buffer.addVertex(matrix, x1 - nx, y1 - ny, 0).setColor(colour);
                buffer.addVertex(matrix, x1 + nx, y1 + ny, 0).setColor(colour);
            }
            graphics.flush();
        }
    }

    // ---------- right-click menu ----------

    private void closeMenu() {
        contextMenu.forEach(this::removeWidget);
        contextMenu.clear();
    }

    private void openMenu(int mouseX, int mouseY) {
        closeMenu();
        Minecraft mc = Minecraft.getInstance();
        int worldX = Mth.floor(viewX + (mouseX - this.width / 2.0) / zoom);
        int worldZ = Mth.floor(viewZ + (mouseY - this.height / 2.0) / zoom);
        boolean canTeleport = mc.player != null && mc.player.hasPermissions(2);
        int w = 150, rows = canTeleport ? 2 : 1;
        int x = Math.min(mouseX, this.width - w - 4), y = Math.min(mouseY, this.height - rows * 22 - 4);
        contextMenu.add(this.addRenderableWidget(new AtlasButton(x, y, w, 20,
                Component.translatable("screen.atlas.menu.add_marker"), null, button -> {
                    closeMenu();
                    addMarkerAt(worldX, worldZ);
                })));
        if (canTeleport) {
            contextMenu.add(this.addRenderableWidget(new AtlasButton(x, y + 22, w, 20,
                    Component.translatable("screen.atlas.menu.teleport"), null, button -> {
                        closeMenu();
                        if (atlasId != null) PacketDistributor.sendToServer(new TeleportPayload(atlasId, dimension, worldX, worldZ));
                        this.onClose();
                    })));
        }
    }

    private void addMarkerAt(int worldX, int worldZ) {
        if (atlasId == null) return;
        Minecraft mc = Minecraft.getInstance();
        int y = mc.player != null ? Mth.floor(mc.player.getY()) : 64;
        if (mc.level != null && dimension.equals(ownDimension) && mc.level.hasChunk(worldX >> 4, worldZ >> 4)) {
            y = mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
        }
        AtlasMarker marker = AtlasMarker.flag(java.util.UUID.randomUUID(), worldX, y, worldZ, -1, "");
        PacketDistributor.sendToServer(new AddMarkerPayload(atlasId, dimension, marker));
        ClientAtlasCache.markers().add(marker);
        pendingPanel = marker;     // opened on the next tick, once this click has been fully handled
        setFocused(null);
    }

    private void closePanel() {
        panelWidgets.forEach(this::removeWidget);
        panelWidgets.clear();
        iconListWidgets.forEach(this::removeWidget);
        iconListWidgets.clear();
        iconListOpen = false;
        editing = null;
        nameBox = xBox = yBox = zBox = null;
        descriptionBox = null;
        iconButton = null;
    }

    private void drawPanel(GuiGraphics graphics) {
        int px = panelX(), top = 8, w = PANEL_W - 16, left = px + 8, ph = this.height - 16;
        plate(graphics, px, top, PANEL_W, ph);
        Component editTitle = Component.translatable("screen.atlas.edit.title");
        graphics.drawString(this.font, editTitle, px + PANEL_W / 2 - this.font.width(editTitle) / 2, top + 14, INK, false);
        graphics.drawString(this.font, Component.translatable("screen.atlas.edit.name"), left, top + 42, INK, false);
        graphics.drawString(this.font, Component.translatable("screen.atlas.edit.icon.label"), left, top + 84, INK, false);
        graphics.drawString(this.font, Component.translatable("screen.atlas.edit.coordinates"), left, top + 130, INK, false);
        graphics.drawString(this.font, Component.translatable("screen.atlas.edit.description"), left, top + 178, INK, false);
        for (int[] r : fieldBackgrounds) {
            graphics.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], 0xFF6B4423);
            graphics.fill(r[0] + 1, r[1] + 1, r[0] + r[2] - 1, r[1] + r[3] - 1, 0xFFF6E7BC);
        }
    }

    /** The small pictures next to the icon button and in the icon list, drawn above the (text-only) buttons. */
    private void drawPanelIcons(GuiGraphics graphics) {
        if (editing == null || iconButton == null) return;
        int size = iconTextureSize(editIcon);
        graphics.blit(iconTexture(editIcon), iconButton.getX() + 4, iconButton.getY() + 3, 16, 16, 0, 0, size, size, size, size);
        int index = 0;
        for (AbstractWidget w : iconListWidgets) {
            String key = AtlasMarker.ICONS.get(index);
            int ks = iconTextureSize(key);
            graphics.blit(iconTexture(key), w.getX() + 4, w.getY() + 2, 16, 16, 0, 0, ks, ks, ks, ks);
            index++;
        }
    }

    /** The strip behind the buttons, the title plate, the coordinates and scale, and the compass. */
    private void drawFurniture(GuiGraphics graphics, int mouseX, int mouseY, double centreX, double centreY) {
        // button strip
        int strip = 4 * (BUTTON + 4) + 4;
        plate(graphics, toolbarX(), toolbarY(), BUTTON + 8, strip);

        int rightButtons = 2 + (CREATE_LOADED ? 1 : 0) + (isCreative() ? 1 : 0);
        plate(graphics, playersX(), toolbarY(), BUTTON + 8, rightButtons * (BUTTON + 4) + 4);

        // title plate: the world's dimension
        Component title = Component.translatable("screen.atlas.plate", dimensionName(dimension));
        int tw = titleW(), tx = titleX(), ty = FRAME + 4;
        plate(graphics, tx, ty, tw, 18);
        graphics.drawString(this.font, title, tx + 10, ty + 5, INK, false);
        // a little arrow: the plate opens the list of dimensions
        int ax = tx + tw - 14, ay = ty + (dropdownOpen ? 9 : 7);
        for (int i = 0; i < 4; i++) {
            int half = dropdownOpen ? i : 3 - i;
            graphics.fill(ax + 3 - half, ay + i, ax + 4 + half, ay + i + 1, INK);
        }

        // coordinates under the cursor, and the scale bar
        int bx = FRAME + 8, bh = 18, by = this.height - FRAME - 8 - bh, bw = 252;
        plate(graphics, bx, by, bw, bh);
        int vx = FRAME, vy = FRAME, vw = this.width - 2 * FRAME, vh = this.height - 2 * FRAME;
        // the two coordinate cells show the spot under the cursor and can be typed into to jump somewhere
        if (gotoX != null && !gotoX.isFocused() && !gotoZ.isFocused()) {
            boolean onMap = mouseX >= vx && mouseX < vx + vw && mouseY >= vy && mouseY < vy + vh && !overPanel(mouseX)
                    && !(mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh);
            double wx = onMap ? viewX + (mouseX - centreX) / zoom : viewX;
            double wz = onMap ? viewZ + (mouseY - centreY) / zoom : viewZ;
            gotoX.setValue(Integer.toString(Mth.floor(wx)));
            gotoZ.setValue(Integer.toString(Mth.floor(wz)));
        }
        smallText(graphics, Component.literal("X"), bx + 8, by + 6);
        smallText(graphics, Component.literal("Z"), bx + 68, by + 6);
        graphics.fill(bx + 15, by + 14, bx + 50, by + 15, 0x55402010);
        graphics.fill(bx + 75, by + 14, bx + 110, by + 15, 0x55402010);
        int[] nice = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000};
        int blocks = 1;
        for (int n : nice) if (n * zoom <= 70) blocks = n;
        int barPx = Math.max(4, Mth.floor(blocks * zoom));
        int barX = bx + 124, barY = by + 9;
        graphics.fill(barX, barY, barX + barPx, barY + 1, INK);
        graphics.fill(barX, barY - 3, barX + 1, barY + 4, INK);
        graphics.fill(barX + barPx - 1, barY - 3, barX + barPx, barY + 4, INK);
        smallText(graphics, Component.translatable("screen.atlas.scale", blocks), bx + 202, by + 6);

        // compass, bottom right (north is always up)
        graphics.blit(COMPASS, this.width - FRAME - 6 - 48, this.height - FRAME - 6 - 48, 0, 0, 48, 48, 48, 48);
    }

    /** The game's own 8x8 map pictures: banner flags (by colour) and the structure icons taken from vanilla. */
    private static boolean isVanillaPicture(AtlasMarker marker) {
        return (AtlasMarker.DEFAULT_ICON.equals(marker.icon()) && marker.color() >= 0)
                || AtlasMarker.VANILLA_ICONS.contains(marker.icon());
    }

    private static ResourceLocation markerTexture(AtlasMarker marker) {
        if (AtlasMarker.VANILLA_ICONS.contains(marker.icon())) {
            return ResourceLocation.withDefaultNamespace("textures/map/decorations/" + marker.icon() + ".png");
        }
        if (AtlasMarker.DEFAULT_ICON.equals(marker.icon()) && marker.color() >= 0) {
            return ResourceLocation.withDefaultNamespace("textures/map/decorations/"
                    + DyeColor.byId(marker.color()).getName() + "_banner.png");
        }
        return icon(marker.icon());
    }

    /** A picture for an icon key in the editor: vanilla ones from the game, the rest the Atlas's own. */
    private static ResourceLocation iconTexture(String key) {
        if (AtlasMarker.VANILLA_ICONS.contains(key)) {
            return ResourceLocation.withDefaultNamespace("textures/map/decorations/" + key + ".png");
        }
        return icon(key);
    }

    private static int iconTextureSize(String key) {
        return AtlasMarker.VANILLA_ICONS.contains(key) ? 8 : 16;
    }

    /** What to call a marker: its name, or for structures the structure's name. */
    private static String displayName(AtlasMarker marker) {
        String name = marker.name();
        if (name.isEmpty() && marker.color() == AtlasMarker.COLOR_DEATH) return Component.translatable("screen.atlas.marker.death").getString();
        if (name.isEmpty()) return Component.translatable("screen.atlas.marker.unnamed").getString();
        if (marker.color() == AtlasMarker.COLOR_STRUCTURE) {
            String key = "atlas.structure." + name.replace(':', '.');
            if (net.minecraft.client.resources.language.I18n.exists(key)) return net.minecraft.client.resources.language.I18n.get(key);
            String path = name.substring(name.indexOf(':') + 1);
            if (path.matches("[a-z0-9_/]+")) {
                path = path.replace('_', ' ').replace('/', ' ');
                return Character.toUpperCase(path.charAt(0)) + path.substring(1);
            }
        }
        return name;
    }

    // ---------- input ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && mouseX >= titleX() && mouseX < titleX() + titleW() && mouseY >= FRAME + 4 && mouseY < FRAME + 22) {
            closeMenu();
            toggleDropdown();
            return true;
        }
        if (playersOpen) {
            togglePlayers();      // a click beside the list closes it
            return true;
        }
        if (!contextMenu.isEmpty()) {
            closeMenu();      // a click beside the menu closes it
            return true;
        }
        if (button == 1 && hoveredMarker == null && hoveredLive == null && !overPanel(mouseX)
                && !dropdownOpen && atlasId != null && mouseX > toolbarX() + BUTTON + 8) {
            openMenu((int) mouseX, (int) mouseY);
            return true;
        }
        if (overPanel(mouseX)) return true;        // clicks on the empty part of the panel do nothing
        if (button == 1 && hoveredMarker != null && !hoveredMarker.id().equals(HOME_ID)) {
            openPanel(hoveredMarker);
            return true;
        }
        if (editing != null) {
            closePanel();      // a click anywhere outside the panel closes it
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (overPanel(mouseX)) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (mode == Mode.FREE && button == 0) {
            viewX -= dragX / zoom;
            viewZ -= dragY / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overPanel(mouseX)) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        double newZoom = Mth.clamp(zoom * Math.pow(1.25, scrollY), MIN_ZOOM, MAX_ZOOM);
        if (mode == Mode.FREE) {
            // keep the point under the cursor where it is while zooming
            double centreX = this.width / 2.0, centreY = this.height / 2.0;
            double worldX = viewX + (mouseX - centreX) / zoom, worldZ = viewZ + (mouseY - centreY) / zoom;
            viewX = worldX - (mouseX - centreX) / newZoom;
            viewZ = worldZ - (mouseY - centreY) / newZoom;
        }
        zoom = newZoom;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox box && (box == gotoX || box == gotoZ)) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                applyGoto();
                setFocused(null);
                return true;
            }
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                setFocused(null);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (editing != null) {
            // while a marker is being edited keys belong to the fields (the open key must not close the screen)
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                if (iconListOpen) toggleIconList(); else closePanel();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (AtlasKey.OPEN.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return editing == null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
