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
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public final class ConfigMenuScreen extends CategoryListScreen {

    @SuppressWarnings("unchecked")
    public ConfigMenuScreen(Screen parent) {
        super(Component.literal(Translation.get("socksproxyclient.config")), parent);

        ImmutableList.Builder listBuilder = ImmutableList.builder().add(
            new Category(Component.literal(Translation.get("socksproxyclient.config.base")), (client, title, x, y, width, height) -> Button.builder(title, _ -> client.setScreen(new BaseConfigScreen(this))).bounds(x, y, width, height).build()),
            new Category(Component.literal(Translation.get("socksproxyclient.config.proxy.test")), (client, title, x, y, width, height) -> Button.builder(title, _ -> client.setScreen(new TestProxyScreen(this))).bounds(x, y, width, height).build()),
            new Category(Component.literal(Translation.get("socksproxyclient.config.dns")), (client, title, x, y, width, height) -> Button.builder(title, _ -> client.setScreen(new DNSConfigScreen(this, (resolver, b) -> {
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
            }))).bounds(x, y, width, height).build()),
            new Category(Component.literal(Translation.get("socksproxyclient.config.doh")), (client, title, x, y, width, height) -> Button.builder(title, _ -> client.setScreen(new ChooseDOHProviderScreen(this, (p, u) -> {
                try {
                    SocksProxyClientConfig dnsConfig = ConfigUtils.getConfigInstance(DNSOverHTTPSConfig.class);
                    dnsConfig.getEntryField("dohProvider", DNSOverHTTPSProvider.class).setValue(p);
                    dnsConfig.getEntryField("customDohProvider", String.class).setValue(u);
                    dnsConfig.save();
                } catch (Throwable e) {
                    throw new Error(e);
                }
                LogUtils.logDebug("doh provider: {}, custom url: {}", p.getDisplayName(), u);
            }))).bounds(x, y, width, height).build())
        );
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Category[] debugs = new Category[30];
            Arrays.fill(debugs, new Category(Component.translatable("key.category.minecraft.debug"), (client, title, x, y, width, height) -> Button.builder(title, _ -> client.setScreen(new TestProxyScreen(this))).bounds(x, y, width, height).build()));
            listBuilder.addAll(Arrays.asList(debugs));
        }
        this.categories = listBuilder.build();
    }
}
