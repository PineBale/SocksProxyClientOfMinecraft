package dev.pinebale.minecraft.fabric.socksproxyclient.doh.screen;

import dev.pinebale.minecraft.fabric.socksproxyclient.dns.DNSUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.DNSOverHTTPSProvider;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.DNSOverHTTPSResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.DNSOverHTTPSUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public final class ChooseDOHProviderScreen extends Screen {

    private final Screen parent;

    private DNSOverHTTPSProvider dohSelection;
    private String customUrlSelection;

    private CycleButton<DNSOverHTTPSProvider> dohSelectionButton;
    private EditBox customUrlField;

    private final BiConsumer<DNSOverHTTPSProvider, String> callback;

    public ChooseDOHProviderScreen(
        @NonNull final Screen parent,
        @NonNull final BiConsumer<DNSOverHTTPSProvider, String> callback
    ) {
        super(Component.literal(Translation.get("socksproxyclient.config.doh")));
        this.parent = parent;
        try {
            this.dohSelection = DNSOverHTTPSUtils.getProvider();
            this.customUrlSelection = DNSOverHTTPSUtils.getCustomUrl();
        } catch (Throwable e) {
            throw new Error(e);
        }
        this.callback = callback;
    }

    @Override
    protected void init() {
        this.dohSelectionButton = CycleButton.builder(v -> Component.literal(v.getDisplayName()), this.dohSelection).withValues(DNSOverHTTPSProvider.values()).create(this.width / 2 - 100, 86, 200, 20, Component.literal(Translation.get("socksproxyclient.config.doh.provider")), (_, _) -> this.updateUrlField());
        this.addRenderableWidget(this.dohSelectionButton);

        this.customUrlField = new EditBox(this.font, this.width / 2 - 100, 126, 200, 20, Component.empty());
        this.customUrlField.setValue(this.customUrlSelection);
        this.addWidget(this.customUrlField);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
            .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        try {
            if (!DNSUtils.getResolverClass().equals(DNSOverHTTPSResolver.class)) {
                this.dohSelectionButton.active = false;
                this.customUrlField.active = false;
                this.customUrlField.setEditable(false);
                return;
            }
        } catch (Throwable e) {
            throw new Error(e);
        }

        this.updateUrlField();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.dohSelectionButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, 17, -1);
        graphics.text(this.font, Component.literal(Translation.get("socksproxyclient.config.doh.customUrl")), this.width / 2 - 100 + 1, 114, -6250336);
        this.customUrlField.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        this.callback.accept(this.dohSelectionButton.getValue(), this.customUrlField.getValue());
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void resize(int width, int height) {
        this.dohSelection = this.dohSelectionButton.getValue();
        this.customUrlSelection = this.customUrlField.getValue();
        super.resize(width, height);
    }

    private void updateUrlField() {
        boolean v = this.dohSelectionButton.getValue().equals(DNSOverHTTPSProvider.CUSTOM);
        this.customUrlField.active = v;
        this.customUrlField.setEditable(v);
    }
}
