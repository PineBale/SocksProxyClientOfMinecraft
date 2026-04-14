package dev.pinebale.minecraft.fabric.socksproxyclient.screen;

import com.google.common.collect.ImmutableList;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.config.DNSConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.screen.DNSConfigScreen;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.DNSOverHTTPSProvider;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.config.DNSOverHTTPSConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.screen.ChooseDOHProviderScreen;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

// TODO: FIX
@Environment(EnvType.CLIENT)
public final class SettingsMenuScreen extends Screen {
    private final Screen parent;

    private double scrollAmount = 0.0;
    private double maxScroll = 0.0;

    private boolean draggingScrollBar = false;
    private double dragStartMouseY = 0.0;
    private double dragStartScrollAmount = 0.0;

    private static final int buttonWidth = 150;
    private static final int buttonHeight = 20;
    private static final int spacing = 10;
    private static final int columns = 2;

    private final List<Button> buttons = new ArrayList<>();
    private final ImmutableList<Category> categories;

    private record Category(String title, Supplier<Screen> subScreenSupplier) {}

    public SettingsMenuScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;

        ImmutableList.Builder listBuilder = ImmutableList.builder().add(
            new Category("Test Proxy", () -> new TestProxyScreen(this)),
            new Category("DNS Config", () -> new DNSConfigScreen(this, (resolver, b) -> {
                try {
                    SocksProxyClientConfig dnsConfig = ConfigUtils.getConfigInstance(DNSConfig.class);
                    dnsConfig.getEntryField("resolver", Class.class).setValue(resolver);
                    dnsConfig.getEntryField("shouldDismissSystemHosts", Boolean.class).setValue(b);
                    dnsConfig.save();
                } catch (Throwable e) {
                    throw new Error(e);
                }
                // https://stackoverflow.com/questions/15202997/what-is-the-difference-between-canonical-name-simple-name-and-class-name-in-jav
                LogUtils.logDebug("resolver: {} shouldDismissSystemHosts: {}", resolver.getName(), b);
            })),
            new Category("DNS-Over-HTTPS Config", () -> new ChooseDOHProviderScreen(this, (p, u) -> {
                try {
                    SocksProxyClientConfig dnsConfig = ConfigUtils.getConfigInstance(DNSOverHTTPSConfig.class);
                    dnsConfig.getEntryField("dohProvider", DNSOverHTTPSProvider.class).setValue(p);
                    dnsConfig.getEntryField("customDohProvider", String.class).setValue(u);
                    dnsConfig.save();
                } catch (Throwable e) {
                    throw new Error(e);
                }
                LogUtils.logDebug("doh provider: {} custom url: {}", p.getDisplayName(), u);
            }))
        );
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Category[] debugs = new Category[30];
            Arrays.fill(debugs, new Category("Debug", () -> new TestProxyScreen(this)));
            listBuilder.addAll(Arrays.asList(debugs));
        }
        this.categories = listBuilder.build();
    }

    @Override
    protected void init() {
        this.buttons.clear();
        this.draggingScrollBar = false;
        this.dragStartMouseY = 0.0;
        this.dragStartScrollAmount = 0.0;

        int startY = 40;
        int rowHeight = buttonHeight + spacing;

        int numRows = (this.categories.size() + columns - 1) / columns;

        int bottomReserved = 50;
        int visibleHeight = this.height - 40 - bottomReserved;
        int contentHeight = startY + numRows * rowHeight;

        double oldMaxScroll = this.maxScroll;
        this.maxScroll = Math.max(0, contentHeight - visibleHeight);

        if (oldMaxScroll > 0) {
            this.scrollAmount = this.scrollAmount / oldMaxScroll * this.maxScroll;
        } else {
            this.scrollAmount = 0.0;
        }

        int leftX = this.width / 2 - 155;
        int rightX = this.width / 2 + 5;

        for (int i = 0; i < this.categories.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = col == 0 ? leftX : rightX;
            int baseY = startY + row * rowHeight;

            final int v = i;
            Button button = Button.builder(
                Component.literal(this.categories.get(v).title()),
                _ -> this.minecraft.setScreen(categories.get(v).subScreenSupplier.get())
            ).bounds(x, baseY, buttonWidth, buttonHeight).build();

            this.addRenderableWidget(button);
            this.buttons.add(button);
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
            .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.updateButtonPositions();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void updateButtonPositions() {
        int leftX = this.width / 2 - 155;
        int rightX = this.width / 2 + 5;
        int startY = 40;
        int rowHeight = buttonHeight + spacing;

        for (int i = 0; i < this.buttons.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int baseY = startY + row * rowHeight;
            int y = (int) (baseY - this.scrollAmount);

            Button btn = this.buttons.get(i);
            btn.setX(col == 0 ? leftX : rightX);
            btn.setY(y);
        }
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.maxScroll > 0) {
            int scrollbarX = this.width - 10;
            int scrollbarY = 40;
            int scrollbarHeight = this.height - 80;
            double thumbHeight = Math.clamp((double) scrollbarHeight / (this.maxScroll + scrollbarHeight) * scrollbarHeight, 10, scrollbarHeight);
            int thumbY = scrollbarY + (int) (this.scrollAmount / this.maxScroll * (scrollbarHeight - thumbHeight));

            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0xFF555555);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + (int) thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
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
            int scrollbarX = this.width - 10;
            int scrollbarY = 40;
            int scrollbarHeight = this.height - 80;
            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6 &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {

                double thumbHeight = Math.clamp((double) scrollbarHeight / (this.maxScroll + scrollbarHeight) * scrollbarHeight, 10, scrollbarHeight);
                int thumbY = scrollbarY + (int) (this.scrollAmount / this.maxScroll * (scrollbarHeight - thumbHeight));

                if (mouseY >= thumbY && mouseY <= thumbY + (int) thumbHeight) {
                    this.draggingScrollBar = true;
                    this.dragStartMouseY = mouseY;
                    this.dragStartScrollAmount = this.scrollAmount;
                    return true;
                } else {
                    double clickRatio = (mouseY - scrollbarY) / scrollbarHeight;
                    this.scrollAmount = Math.clamp(clickRatio * this.maxScroll, 0.0, this.maxScroll);
                    this.updateButtonPositions();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.draggingScrollBar && this.maxScroll > 0) {
            int scrollbarHeight = this.height - 80;
            double thumbHeight = Math.clamp((double) scrollbarHeight / (this.maxScroll + scrollbarHeight) * scrollbarHeight, 10, scrollbarHeight);
            double scrollTrackHeight = scrollbarHeight - thumbHeight;

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
