package dev.pinebale.minecraft.fabric.socksproxyclient.screen;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// Created with the help of Gemini and Grok.
// TODO: MORE MANUAL TEST
@Environment(EnvType.CLIENT)
public abstract class CategoryListScreen extends Screen {
    protected final Screen parent;

    private static final int buttonWidth = 150;
    private static final int buttonHeight = 20;
    private static final int buttonRowSpacing = 10;
    private static final int buttonColumns = 2;

    private int leftColumnX = 0;
    private int rightColumnX = 0;

    private static final int headerCut = 40;
    private static final int footerCut = 50;

    private int scrollBarX = 0;
    private int scrollBarY = 0;
    private double scrollBarHeight = 0.0;

    private double scrollAmount = 0.0;
    private double maxScroll = 0.0;
    private boolean draggingScrollBar = false;
    private double dragStartMouseY = 0.0;
    private double dragStartScrollAmount = 0.0;

    @FunctionalInterface
    protected interface CategoryButtonSupplier {
        AbstractButton get(final Minecraft minecraft, final Component component, final int x, final int y, final int width, final int height);
    }
    protected record Category(Component title, CategoryButtonSupplier buttonSupplier) {}

    protected final List<AbstractButton> buttons = new ArrayList<>();
    protected ImmutableList<Category> categories;

    protected CategoryListScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.buttons.clear();

        this.leftColumnX = this.width / 2 - 155;
        this.rightColumnX = this.width / 2 + 5;

        this.scrollBarX = this.width - 10;
        this.scrollBarY = headerCut;
        this.scrollBarHeight = this.height - headerCut - footerCut;

        this.draggingScrollBar = false;
        this.dragStartMouseY = 0.0;
        this.dragStartScrollAmount = 0.0;

        final int startY = headerCut;
        final int rowHeight = buttonHeight + buttonRowSpacing;
        final int numRows = (this.categories.size() + buttonColumns - 1) / buttonColumns;

        final int visibleHeight = this.height - headerCut - footerCut;
        final int contentHeight = startY + numRows * rowHeight;

        final double oldMaxScroll = this.maxScroll;
        this.maxScroll = Math.max(0, contentHeight - visibleHeight);

        if (oldMaxScroll > 0) {
            this.scrollAmount = this.scrollAmount / oldMaxScroll * this.maxScroll;
        } else {
            this.scrollAmount = 0.0;
        }

        for (int i = 0; i < this.categories.size(); i++) {
            final int col = i % buttonColumns;
            final int row = i / buttonColumns;
            final int x = col == 0 ? this.leftColumnX : this.rightColumnX;
            final int baseY = startY + row * rowHeight;

            AbstractButton button = this.categories.get(i).buttonSupplier.get(this.minecraft, this.categories.get(i).title(), x, baseY, buttonWidth, buttonHeight);

            this.addWidget(button);
            this.buttons.add(button);
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.updateButtonPositions();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void updateButtonPositions() {
        final int startY = headerCut;
        final int rowHeight = buttonHeight + buttonRowSpacing;

        for (int i = 0; i < this.buttons.size(); i++) {
            final int col = i % buttonColumns;
            final int row = i / buttonColumns;
            final int baseY = startY + row * rowHeight;
            final int y = (int) (baseY - this.scrollAmount);

            AbstractButton btn = this.buttons.get(i);
            btn.setX(col == 0 ? this.leftColumnX : this.rightColumnX);
            btn.setY(y);

            btn.active = y > headerCut - buttonHeight && y < this.height - footerCut;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        graphics.enableScissor(0, headerCut, this.width, this.height - footerCut);
        super.extractMenuBackground(graphics);
        for (AbstractButton btn : this.buttons) {
            btn.extractRenderState(graphics, mouseX, mouseY, a);
        }
        graphics.disableScissor();
        graphics.horizontalLine(0, this.width, headerCut - 1, 0xFF000000);
        graphics.horizontalLine(0, this.width, this.height - footerCut, 0xFF000000);

        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, 17, -1);

        if (this.maxScroll > 0) {
            final double thumbHeight = Math.clamp(this.scrollBarHeight / (this.maxScroll + this.scrollBarHeight) * this.scrollBarHeight, 10, this.scrollBarHeight);
            int thumbHeightFloor = (int) thumbHeight;
            if (thumbHeightFloor != thumbHeight) {
                thumbHeightFloor = thumbHeightFloor - (int) (Double.doubleToRawLongBits(thumbHeight) >>> 63);
            }

            int thumbY = this.scrollBarY + (int) (this.scrollAmount / this.maxScroll * (this.scrollBarHeight - thumbHeightFloor));

            graphics.fill(this.scrollBarX, this.scrollBarY, this.scrollBarX + 6, (int) (this.scrollBarY + this.scrollBarHeight), 0xFF555555);
            graphics.fill(this.scrollBarX, thumbY, this.scrollBarX + 6, thumbY + thumbHeightFloor, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll > 0) {
            double oldScroll = this.scrollAmount;
            this.scrollAmount = Math.clamp(this.scrollAmount - verticalAmount * 20, 0.0, this.maxScroll);
            if (oldScroll != this.scrollAmount) {
                this.updateButtonPositions();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && maxScroll > 0) {
            final double mouseX = event.x();
            final double mouseY = event.y();
            if (mouseX >= this.scrollBarX && mouseX <= this.scrollBarX + 6 &&
                    mouseY >= this.scrollBarY && mouseY <= this.scrollBarY + this.scrollBarHeight) {

                double thumbHeight = Math.clamp(this.scrollBarHeight / (this.maxScroll + this.scrollBarHeight) * this.scrollBarHeight, 10, this.scrollBarHeight);
                int thumbY = this.scrollBarY + (int) (this.scrollAmount / this.maxScroll * (this.scrollBarHeight - thumbHeight));

                if (mouseY >= thumbY && mouseY <= thumbY + (int) thumbHeight) {
                    this.draggingScrollBar = true;
                    this.dragStartMouseY = mouseY;
                    this.dragStartScrollAmount = this.scrollAmount;
                } else {
                    double clickRatio = (mouseY - this.scrollBarY) / this.scrollBarHeight;
                    this.scrollAmount = Math.clamp(clickRatio * this.maxScroll, 0.0, this.maxScroll);
                    this.updateButtonPositions();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.draggingScrollBar && this.maxScroll > 0) {
            double thumbHeight = Math.clamp(this.scrollBarHeight / (this.maxScroll + this.scrollBarHeight) * this.scrollBarHeight, 10, this.scrollBarHeight);
            double scrollTrackHeight = this.scrollBarHeight - thumbHeight;

            double deltaScroll = (event.y() - this.dragStartMouseY) * (this.maxScroll / scrollTrackHeight);
            this.scrollAmount = Math.clamp(this.dragStartScrollAmount + deltaScroll, 0.0, this.maxScroll);

            this.updateButtonPositions();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.draggingScrollBar = false;
        }
        return super.mouseReleased(event);
    }
}
