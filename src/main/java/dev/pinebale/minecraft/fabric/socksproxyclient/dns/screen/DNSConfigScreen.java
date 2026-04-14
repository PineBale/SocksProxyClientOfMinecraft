package dev.pinebale.minecraft.fabric.socksproxyclient.dns.screen;

import dev.pinebale.minecraft.fabric.socksproxyclient.dns.DNSUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SocksProxyClientDNSResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SystemResolver;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
@Environment(EnvType.CLIENT)
public final class DNSConfigScreen extends Screen {

    private final Screen parent;

    private final Class initialResolverSelection;
    private final boolean initialShouldDismissHosts;

    private CycleButton<Class> resolverButton;
    private CycleButton<Boolean> dismissHostsButton;

    private final BiConsumer<Class<? extends SocksProxyClientDNSResolver>, Boolean> callback;

    public DNSConfigScreen(
        @NonNull final Screen parent,
        @NonNull final BiConsumer<Class<? extends SocksProxyClientDNSResolver>, Boolean> callback
    ) {
        super(Component.literal("DNS config"));
        this.parent = parent;
        try {
            this.initialResolverSelection = DNSUtils.getResolverClass();
            this.initialShouldDismissHosts = DNSUtils.shouldDismissSystemHosts();
        } catch (Throwable e) {
            throw new Error(e);
        }
        this.callback = callback;
    }

    private static final List<Class> resolvers = new ArrayList<>();

    static {
        for (SocksProxyClientDNSResolver s : ServiceLoader.load(SocksProxyClientDNSResolver.class, SocksProxyClientDNSResolver.class.getClassLoader())) {
            resolvers.add(s.getClass());
        }
    }

    @Override
    protected void init() {
        this.resolverButton = CycleButton.builder(v -> {
            try {
                return Component.literal(DNSUtils.getResolverName(v));
            } catch (Exception e) {
                throw new Error(e);
            }
        }, this.initialResolverSelection).withValues(resolvers).create(this.width / 2 - 100, 86, 200, 20, Component.literal("DNS resolver"), (_, _) -> this.updateDismissHostsButton());
        this.addRenderableWidget(this.resolverButton);

        this.dismissHostsButton = CycleButton.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.GUI_NO, this.initialShouldDismissHosts).create(this.width / 2 - 100, 126, 200, 20, Component.literal("Should dismiss system hosts"));
        this.addRenderableWidget(this.dismissHostsButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
            .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.updateDismissHostsButton();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, 17, -1);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(resolverButton);
    }

    @Override
    public void onClose() {
        this.callback.accept(this.resolverButton.getValue(), this.dismissHostsButton.getValue());
        this.minecraft.setScreen(this.parent);
    }

    private void updateDismissHostsButton() {
        this.dismissHostsButton.active = !this.resolverButton.getValue().equals(SystemResolver.class);
    }
}
