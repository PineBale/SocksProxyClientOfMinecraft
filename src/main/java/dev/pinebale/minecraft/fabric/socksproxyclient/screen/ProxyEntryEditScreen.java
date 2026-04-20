package dev.pinebale.minecraft.fabric.socksproxyclient.screen;

import com.google.common.net.HostAndPort;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.ProxyEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.SocksProxyCredential;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("FieldMayBeFinal")
@Environment(EnvType.CLIENT)
// FIXME: TO BE USED LATER.
public final class ProxyEntryEditScreen extends Screen {
    private final Screen parent;

    /**
     * It's a pointer to new object.
     */
    @NonNull
    private ProxyEntry entry;

    /* Host and port */
    private EditBox proxyAddressField;
    private EditBox usernameField;
    private EditBox passwordField;
    private Button setButton;

    private final Runnable callback;

    public ProxyEntryEditScreen(final Screen parent, @NonNull ProxyEntry entry, @NonNull final Runnable callback) {
        super(Component.empty());
        this.parent = parent;
        this.entry = entry;
        this.callback = callback;
    }

    @Override
    protected void init() {
        this.proxyAddressField = new EditBox(this.font, this.width / 2 - 100, 46, 200, 20, Component.empty());
        this.proxyAddressField.setMaxLength(262);
        String p = "";
        this.proxyAddressField.setValue(p);
        InetSocketAddress sa = (InetSocketAddress) entry.getProxy().address();
        String saStr = sa.getHostString();
        if (saStr != null) {
            p = saStr + ":" + sa.getPort();
        }
        this.proxyAddressField.setValue(p);
        this.proxyAddressField.setResponder(_ -> this.updateSetButton());
        this.addWidget(this.proxyAddressField);

        this.usernameField = new EditBox(this.font, this.width / 2 - 100, 86, 200, 20, Component.empty());
        this.usernameField.setMaxLength(255);
        this.usernameField.setValue(Objects.requireNonNullElse(entry.getSocksProxyCredential().username(), ""));
        this.addWidget(this.usernameField);

        this.passwordField = new EditBox(this.font, this.width / 2 - 100, 126, 200, 20, Component.empty());
        this.passwordField.setMaxLength(255);
        this.passwordField.addFormatter((string, _) -> FormattedCharSequence.forward("*".repeat(string.length()), Style.EMPTY));
        this.passwordField.setValue(Objects.requireNonNullElse(entry.getSocksProxyCredential().password(), ""));
        this.addWidget(this.passwordField);

        this.setButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.setAndClose())
            .bounds(this.width / 2 - 100, this.height / 4 + 116 + 18, 200, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, _ -> this.onClose())
            .bounds(this.width / 2 - 100, this.height / 4 + 140 + 18, 200, 20).build());

        this.updateSetButton();
    }

    @Override
    public void resize(int width, int height) {
        String string = this.proxyAddressField.getValue();
        String string2 = this.usernameField.getValue();
        String string3 = this.passwordField.getValue();
        this.init(width, height);
        this.proxyAddressField.setValue(string);
        this.usernameField.setValue(string2);
        this.passwordField.setValue(string3);
    }

    @Override
    public void onClose() {
        if (this.callback != null) {
            this.callback.run();
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.text(this.font, Component.literal(Translation.get("socksproxyclient.config.proxy.editing.address")), this.width / 2 - 100 + 1, 33, -6250336);
        graphics.text(this.font, Component.literal(Translation.get("socksproxyclient.config.proxy.editing.username")), this.width / 2 - 100 + 1, 74, -6250336);
        graphics.text(this.font, Component.literal(Translation.get("socksproxyclient.config.proxy.editing.password")), this.width / 2 - 100 + 1, 115, -6250336);
        this.proxyAddressField.extractRenderState(graphics, mouseX, mouseY, a);
        this.usernameField.extractRenderState(graphics, mouseX, mouseY, a);
        this.passwordField.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void setAndClose() {
        HostAndPort hostAndPort = HostAndPort.fromString(this.proxyAddressField.getValue());
        entry.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort())));
        entry.setSocksProxyCredential(new SocksProxyCredential(this.usernameField.getValue(), this.passwordField.getValue()));
        this.onClose();
    }

    private void updateSetButton() {
        this.setButton.active = ((Supplier<Boolean>) () -> {
            try {
                return ConfigUtils.addressValidity.test(this.proxyAddressField.getValue());
            } catch (Exception e) {
                // No op
            }
            return false;
        }).get();
    }
}
