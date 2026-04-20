package dev.pinebale.minecraft.fabric.socksproxyclient.screen;

import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

// TODO: LATER
@Environment(EnvType.CLIENT)
public final class BaseConfigScreen extends Screen {
    private final Screen parent;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);

    public BaseConfigScreen(Screen parent) {
        super(Component.literal(Translation.get("socksproxyclient.config.base")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        footer.defaultCellSetting().alignHorizontallyCenter();
        LinearLayout bottomFooterButtons = footer.addChild(LinearLayout.horizontal().spacing(4));

        bottomFooterButtons.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose()).width(74).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
