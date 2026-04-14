package dev.pinebale.minecraft.fabric.socksproxyclient.proxy;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.BaseConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.ProxyEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks5ProxyHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocksUtils {

    @SuppressWarnings("unchecked")
    public static Supplier<List<ProxyEntry>> supplier() {
        return () -> {
            try {
                return (List<ProxyEntry>) ConfigUtils.getEntryField(BaseConfig.class, "proxies").getValue();
            } catch (Exception e) {
                throw new Error(e);
            }
        };
    }

    public static void applyProxyChain(InetSocketAddress remote, ChannelPipeline pipeline, Supplier<List<ProxyEntry>> supplier) {
        applyProxyChain(remote, pipeline, supplier, false);
    }

    public static void applyProxyChain(InetSocketAddress remote, ChannelPipeline pipeline, Supplier<List<ProxyEntry>> supplier, boolean silence) {
        if (remote == null) {
            LogUtils.logDebug("fire: remote is null");
            return;
        }
        applyProxyChain(remote, supplier.get(), pipeline, silence);
    }

    private static void applyProxyChain(@NonNull InetSocketAddress remote, @NonNull List<ProxyEntry> proxies, @NonNull ChannelPipeline pipeline, boolean silence) {
        for (int i = proxies.size() - 1; i >= 0; --i) {
            ProxyEntry entry = proxies.get(i);
            pipeline.addFirst("spc-socks-" + i, new Socks5ProxyHandler(entry.getProxy().address(), entry.getSocksProxyCredential().username(), entry.getSocksProxyCredential().password()));
        }
        if (!silence) {
            logInfo(proxies, remote);
        }
    }

    private static void logInfo(@NonNull List<ProxyEntry> proxies, @NonNull InetSocketAddress remote) {
        Objects.requireNonNull(remote.getHostString());
        if (proxies.isEmpty()) {
            LogUtils.logInfo("[Client] -> [Remote] {}:{}", remote.getHostString(), remote.getPort());
            return;
        }
        StringBuilder builder = new StringBuilder("[Client] -> ");
        for (ProxyEntry entry : proxies) {
            builder.append(String.format("%s:%s -> ",
                ((InetSocketAddress) entry.getProxy().address()).getHostString(),
                ((InetSocketAddress) entry.getProxy().address()).getPort()));
        }
        builder.append(String.format("[Remote] %s:%s", remote.getHostString(), remote.getPort()));
        LogUtils.logInfo("{}", builder);
    }
}
