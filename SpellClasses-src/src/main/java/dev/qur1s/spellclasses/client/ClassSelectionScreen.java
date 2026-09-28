package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.qur1s.spellclasses.ClassSchools;
import dev.qur1s.spellclasses.network.ChooseClassPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opened from the class book. Renders one card per still-pickable school in a 4-then-3 grid,
 * each shaped like a flat-bottomed "shield" gem tinted with the class's own accent color,
 * topped with its grimoire icon and its lore text.
 */
public class ClassSelectionScreen extends Screen {
    private static final int SIDE_MARGIN = 20;
    private static final int GAP = 8;
    private static final int ROW_GAP = -30;
    private static final int TOP_MARGIN = 36;
    private static final float BASE_TEXT_SCALE = 0.75f;
    private static final float MIN_TEXT_SCALE = 0.45f;
    /** Purely visual bump applied at render time on top of the scale used for layout/wrapping - fills the card's existing slack without resizing anything. */
    private static final float TEXT_RENDER_BOOST = 1.15f;
    private static final int ICON_BLOCK_HEIGHT = 30;
    private static final int TITLE_LINE_HEIGHT = 10;
    private static final int GAP_AFTER_TITLE = 4;
    private static final int LINE_HEIGHT = 9;
    private static final int BOTTOM_PAD = 6;
    /** Fraction of card height, from the top, reserved so content clears the blunt top corner. */
    private static final float TOP_INSET_FRACTION = 0.18f;
    /** Fraction of card height (from the top) where the flat bottom edge sits - content must end before it. */
    private static final float BOTTOM_FRACTION = 0.82f;
    /** Fraction of card width safely usable by content at every point inside the content zone. */
    private static final float CONTENT_WIDTH_FRACTION = 0.58f;

    /**
     * Flat-bottomed "shield" gem outline, clockwise from the (blunt) top point, as fractions of
     * card width/height. The waist sits low (0.66) so the diagonals into the flat bottom are short.
     */
    private static final float[] SHAPE_FX = {0.5f, 0.82f, 1f, 0.82f, 0.18f, 0f, 0.18f};
    private static final float[] SHAPE_FY = {0.11f, 0.24f, 0.66f, 0.85f, 0.85f, 0.66f, 0.24f};

    private boolean previousHideGui;

    public ClassSelectionScreen() {
        super(Component.translatable("spellclasses.screen.title"));
    }

    @Override
    protected void init() {
        this.previousHideGui = this.minecraft.options.hideGui;
        this.minecraft.options.hideGui = true;

        List<ResourceLocation> schools = orderedSchools();
        if (schools.isEmpty()) return;

        List<Integer> rowSizes = schools.size() > 4 ? List.of(4, schools.size() - 4) : List.of(schools.size());
        int maxRowSize = rowSizes.get(0);
        int available = this.width - SIDE_MARGIN * 2;
        int maxCardWidth = Math.max(90, (available - (maxRowSize - 1) * GAP) / maxRowSize);
        int availableHeight = Math.max(120, this.height - TOP_MARGIN - 16);
        float usableFraction = BOTTOM_FRACTION - TOP_INSET_FRACTION;

        int cardWidth = Math.min(230, maxCardWidth);
        float textScale = BASE_TEXT_SCALE;
        int wrapWidth = 0;
        int maxDescLines = 1;
        int cardHeight = 0;
        int totalHeight = 0;

        // Pass 1: grow card width (up to the screen limit) to shrink the description line count.
        while (true) {
            wrapWidth = (int) ((cardWidth * CONTENT_WIDTH_FRACTION) / textScale);
            maxDescLines = 1;
            for (ResourceLocation school : schools) {
                maxDescLines = Math.max(maxDescLines, wrapDesc(school, wrapWidth).size());
            }
            cardHeight = cardHeightFor(maxDescLines, textScale, usableFraction);
            totalHeight = rowSizes.size() * cardHeight + (rowSizes.size() - 1) * ROW_GAP;
            if (totalHeight <= availableHeight || cardWidth >= maxCardWidth) break;
            cardWidth = Math.min(maxCardWidth, cardWidth + 10);
        }

        // Pass 2: still too tall for the screen at max width - shrink the text itself until both rows fit.
        while (totalHeight > availableHeight && textScale > MIN_TEXT_SCALE) {
            textScale = Math.max(MIN_TEXT_SCALE, textScale - 0.05f);
            wrapWidth = (int) ((cardWidth * CONTENT_WIDTH_FRACTION) / textScale);
            maxDescLines = 1;
            for (ResourceLocation school : schools) {
                maxDescLines = Math.max(maxDescLines, wrapDesc(school, wrapWidth).size());
            }
            cardHeight = cardHeightFor(maxDescLines, textScale, usableFraction);
            totalHeight = rowSizes.size() * cardHeight + (rowSizes.size() - 1) * ROW_GAP;
        }

        int startY = Math.max(TOP_MARGIN, (this.height - totalHeight) / 2 + TOP_MARGIN / 2);

        int index = 0;
        int y = startY;
        for (int rowSize : rowSizes) {
            int rowWidth = rowSize * cardWidth + (rowSize - 1) * GAP;
            int x = (this.width - rowWidth) / 2;
            for (int i = 0; i < rowSize; i++) {
                ResourceLocation school = schools.get(index++);
                this.addRenderableWidget(new ClassCard(x, y, cardWidth, cardHeight, school, wrapWidth, textScale));
                x += cardWidth + GAP;
            }
            y += cardHeight + ROW_GAP;
        }
    }

    /** Stretches cards slightly taller than the content strictly needs, for a leaner silhouette. */
    private static final float HEIGHT_STRETCH = 1.16f;

    private static int cardHeightFor(int maxDescLines, float textScale, float usableFraction) {
        int logicalTextHeight = TITLE_LINE_HEIGHT + GAP_AFTER_TITLE + maxDescLines * LINE_HEIGHT;
        int contentHeight = ICON_BLOCK_HEIGHT + Math.round(logicalTextHeight * textScale) + BOTTOM_PAD;
        return Math.round(contentHeight / usableFraction * HEIGHT_STRETCH);
    }

    private List<FormattedCharSequence> wrapDesc(ResourceLocation school, int wrapWidth) {
        Component desc = Component.translatable("spellclasses.class." + school.getPath() + ".desc");
        return this.font.split(desc, wrapWidth);
    }

    /** Fixed, deterministic layout order (registry iteration order isn't guaranteed). */
    private static List<ResourceLocation> orderedSchools() {
        Map<String, ResourceLocation> byPath = new LinkedHashMap<>();
        for (ResourceLocation school : ClassSchools.restrictedSchools()) {
            byPath.put(school.getPath(), school);
        }
        List<ResourceLocation> ordered = new ArrayList<>();
        for (String path : ClassMeta.BY_SCHOOL_PATH.keySet()) {
            ResourceLocation school = byPath.remove(path);
            if (school != null) ordered.add(school);
        }
        ordered.addAll(byPath.values());
        return ordered;
    }

    private void chooseAndClose(ResourceLocation school) {
        PacketDistributor.sendToServer(new ChooseClassPayload(school.toString()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xD9000000);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("spellclasses.screen.subtitle"), this.width / 2, 24, 0x999999);
    }

    @Override
    public void removed() {
        this.minecraft.options.hideGui = this.previousHideGui;
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static float[][] shapePoints(int x, int y, int w, int h) {
        float[] xs = new float[SHAPE_FX.length];
        float[] ys = new float[SHAPE_FY.length];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = x + SHAPE_FX[i] * w;
            ys[i] = y + SHAPE_FY[i] * h;
        }
        return new float[][]{xs, ys};
    }

    private static boolean pointInPolygon(float px, float py, float[] xs, float[] ys) {
        boolean inside = false;
        for (int i = 0, j = xs.length - 1; i < xs.length; j = i++) {
            if ((ys[i] > py) != (ys[j] > py)
                    && px < (xs[j] - xs[i]) * (py - ys[i]) / (ys[j] - ys[i]) + xs[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static void fillPolygon(GuiGraphics graphics, float[] xs, float[] ys, int argb) {
        float a = (argb >>> 24) / 255f;
        float r = (argb >> 16 & 0xFF) / 255f;
        float g = (argb >> 8 & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        Matrix4f matrix = graphics.pose().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < xs.length; i++) {
            buffer.addVertex(matrix, xs[i], ys[i], 0).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void strokePolygon(GuiGraphics graphics, float[] xs, float[] ys, float thickness, int color) {
        int n = xs.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float dx = xs[j] - xs[i];
            float dy = ys[j] - ys[i];
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 0.0001f) continue;
            dx /= len;
            dy /= len;
            float half = thickness / 2f;
            float nx = -dy * half;
            float ny = dx * half;
            float x1 = xs[i] - dx * half;
            float y1 = ys[i] - dy * half;
            float x2 = xs[j] + dx * half;
            float y2 = ys[j] + dy * half;
            fillPolygon(graphics, new float[]{x1 + nx, x2 + nx, x2 - nx, x1 - nx},
                    new float[]{y1 + ny, y2 + ny, y2 - ny, y1 - ny}, color);
        }
    }

    private class ClassCard extends AbstractWidget {
        private final ResourceLocation school;
        private final ClassMeta.Entry meta;
        private final ItemStack icon;
        private final Component title;
        private final float titleScale;
        private final List<FormattedCharSequence> descLines;
        private final float textScale;
        private final int topInset;

        ClassCard(int x, int y, int w, int h, ResourceLocation school, int wrapWidth, float textScale) {
            super(x, y, w, h, ClassSchools.className(school));
            this.school = school;
            this.meta = ClassMeta.get(school);
            this.icon = new ItemStack(BuiltInRegistries.ITEM.get(meta.icon()));
            this.title = ClassSchools.className(school);
            this.descLines = wrapDesc(school, wrapWidth);
            this.textScale = textScale;
            this.topInset = (int) (h * TOP_INSET_FRACTION);

            // Title never wraps: shrink it (down to a floor) instead, so it always stays on one line.
            int titleWidth = font.width(title);
            float fitScale = titleWidth > 0 ? (wrapWidth * textScale) / titleWidth : textScale;
            this.titleScale = Math.max(MIN_TEXT_SCALE, Math.min(textScale, fitScale));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            int bg = withAlpha(hovered ? meta.color() : darken(meta.color(), 0.35f), hovered ? 0x70 : 0x30);
            int border = withAlpha(hovered ? meta.color() : darken(meta.color(), 0.5f), hovered ? 0xFF : 0x60);

            float[][] shape = shapePoints(getX(), getY(), width, height);
            fillPolygon(graphics, shape[0], shape[1], bg);
            strokePolygon(graphics, shape[0], shape[1], 2f, border);

            int centerX = getX() + width / 2;
            int contentTop = getY() + topInset;

            graphics.pose().pushPose();
            graphics.pose().translate(centerX - 12, contentTop + 2, 0);
            graphics.pose().scale(1.5f, 1.5f, 1f);
            graphics.renderItem(icon, 0, 0);
            graphics.pose().popPose();

            int titleColor = hovered ? meta.color() : darken(meta.color(), 0.55f);
            int descColor = hovered ? 0xCCCCCC : 0x777777;

            graphics.pose().pushPose();
            graphics.pose().translate(centerX, contentTop + ICON_BLOCK_HEIGHT, 0);
            graphics.pose().scale(titleScale * TEXT_RENDER_BOOST, titleScale * TEXT_RENDER_BOOST, 1f);
            graphics.drawCenteredString(font, title, 0, 0, titleColor);
            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().translate(centerX, contentTop + ICON_BLOCK_HEIGHT + Math.round((TITLE_LINE_HEIGHT + GAP_AFTER_TITLE) * textScale), 0);
            graphics.pose().scale(textScale * TEXT_RENDER_BOOST, textScale * TEXT_RENDER_BOOST, 1f);
            int ly = 0;
            for (FormattedCharSequence line : descLines) {
                int lineWidth = font.width(line);
                graphics.drawString(font, line, -lineWidth / 2, ly, descColor, false);
                ly += LINE_HEIGHT;
            }
            graphics.pose().popPose();
        }

        private static int withAlpha(int rgb, int alpha) {
            return (alpha << 24) | (rgb & 0xFFFFFF);
        }

        private static int darken(int rgb, float factor) {
            int r = Math.round((rgb >> 16 & 0xFF) * factor);
            int g = Math.round((rgb >> 8 & 0xFF) * factor);
            int b = Math.round((rgb & 0xFF) * factor);
            return (r << 16) | (g << 8) | b;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            float[][] shape = shapePoints(getX(), getY(), width, height);
            return pointInPolygon((float) mouseX, (float) mouseY, shape[0], shape[1]);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            chooseAndClose(school);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
