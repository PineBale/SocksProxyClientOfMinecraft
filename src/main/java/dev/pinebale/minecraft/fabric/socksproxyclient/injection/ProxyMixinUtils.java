package dev.pinebale.minecraft.fabric.socksproxyclient.injection;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ProxyConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ProxyInternalConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinConnection;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Proxy;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.ProxyUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.SocksUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import io.netty.channel.ChannelPipeline;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Environment(EnvType.CLIENT)
public final class ProxyMixinUtils {

    public static boolean proxyYggdrasil() {
        return booleanProxyConfigField("proxyYggdrasil");
    }

    public static boolean proxyRealmsApi() {
        return booleanProxyConfigField("proxyRealmsApi");
    }

    public static boolean proxyAssetsDownload() {
        return booleanProxyConfigField("proxyAssetsDownload");
    }

    public static boolean proxyBlockListSupplier() {
        return booleanProxyConfigField("proxyBlockListSupplier");
    }

    public static void determineProxySelection(@NonNull Connection connection, @NonNull ChannelPipeline pipeline) {
        ServerData serverData = ((IMixinConnection) connection).socksProxyClient$getServerData();
        Objects.requireNonNull(serverData);
        ServerData.Type serverType = serverData.type();
        Objects.requireNonNull(serverType);
        InetSocketAddress remote = ((IMixinConnection) connection).socksProxyClient$getInetSocketAddress();
        switch (serverType) {
            case LAN -> determineNoProxyApplication(remote, pipeline, true);
            case REALM -> determineProxyApplication(remote, pipeline);
            case OTHER -> {
                if (!((IMixinServerData_Proxy) serverData).socksProxyClient$isUseProxy()) {
                    determineNoProxyApplication(remote, pipeline, false);
                } else if (ProxyUtils.noProxyMinecraftFilter(serverData.ip)) {
                    determineNoProxyApplication(remote, pipeline, false);
                } else {
                    determineProxyApplication(remote, pipeline);
                }
            }
        }
    }

    private static void determineNoProxyApplication(@NonNull InetSocketAddress remote, @NonNull ChannelPipeline pipeline, boolean silence) {
        SocksUtils.applyProxyChain(remote, pipeline, List::of, silence);
    }

    private static void determineProxyApplication(@NonNull InetSocketAddress remote, @NonNull ChannelPipeline pipeline) {
        SocksUtils.applyProxyChain(remote, pipeline, ProxyUtils.supplierForMinecraft());
    }

    public static CycleButton<Boolean> createOptoutButton(Screen screen, ServerData serverData, int x, int y, int width, int height) {
        boolean v = ProxyUtils.proxyMinecraft();
        boolean initial;
        if (!v) {
            initial = true;
        } else if (screen instanceof DirectJoinServerScreen) {
            try {
                initial = !((Boolean) ConfigUtils.getEntryField(ProxyInternalConfig.class, "directConnectUseProxy").getValue());
            } catch (Exception e) {
                throw new Error(e);
            }
            ((IMixinServerData_Proxy) serverData).socksProxyClient$setUseProxy(!initial);
        } else {
            initial = !((IMixinServerData_Proxy) serverData).socksProxyClient$isUseProxy();
        }
        CycleButton.Builder<Boolean> builder = CycleButton.builder(o -> o ? Component.translatable("gui.yes").withStyle(ChatFormatting.GREEN) : Component.translatable("gui.none").withStyle(ChatFormatting.GRAY), initial);
        builder.withValues(true, false);
        CycleButton<Boolean> buttonWidget = builder.create(x, y, width, height, Component.literal(Translation.get("socksproxyclient.config.base.optout.enabled")), (_, value) -> ((IMixinServerData_Proxy) serverData).socksProxyClient$setUseProxy(!value));
        buttonWidget.setTooltip(Tooltip.create(Component.translatable("gui.yes").append(Component.literal(": " + Translation.get("socksproxyclient.config.base.optout.enabled") + "\n")).append(Component.translatable("gui.none").append(Component.literal(Translation.get("socksproxyclient.config.base.optout.none"))))));
        buttonWidget.active = v;
        return buttonWidget;
    }

    private static boolean booleanProxyConfigField(String field) {
        LogUtils.logDebug("booleanProxyConfigField: {}", field);
        try {
            final boolean v = (Boolean) ConfigUtils.getEntryField(ProxyConfig.class, field).getValue();
            LogUtils.logDebug("booleanProxyConfigField {}: {}", field, v);
            return v;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
