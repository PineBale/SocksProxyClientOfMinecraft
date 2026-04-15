package dev.pinebale.minecraft.fabric.socksproxyclient.proxy;

import com.google.common.net.InetAddresses;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ProxyConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.ProxyEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.BaseUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.tuple.Pair;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Environment(EnvType.CLIENT)
public final class ProxyUtils {

    /**
     * Opt out: Return true.
     */
    public static final Function<Supplier<Pair<String, List<String>>>, Boolean> noProxyMinecraftFilterFunc = (supplier) -> {
        String input = supplier.get().getLeft();
        Objects.requireNonNull(input);
        List<String> settings = supplier.get().getRight();
        for (final String entry : settings) {
            try {
                InetAddress inputIPObject = InetAddresses.forString(input);
                if (inputIPObject instanceof Inet6Address) {
                    return false;
                }
                final boolean v = BaseUtils.isIpInCidr(inputIPObject, entry);
                LogUtils.logDebug("isIpInCidr: {}", v);
                if (entry.contains("/") && v) {
                    return true;
                } else {
                    InetAddress entryIPObject = InetAddresses.forString(entry);
                    if (entryIPObject instanceof Inet6Address) {
                        return false;
                    }
                    if (entryIPObject.equals(inputIPObject)) {
                        return true;
                    }
                }
            } catch (Throwable e) {
                if (("." + input).endsWith("." + entry)) {
                    return true;
                }
            }
        }
        return false;
    };

    @SuppressWarnings("unchecked")
    public static boolean noProxyMinecraftFilter(String input) {
        try {
            List<String> v = (List<String>) ConfigUtils.getEntryField(ProxyConfig.class, "noProxyMinecraft").getValue();
            return noProxyMinecraftFilterFunc.apply(() -> Pair.of(input, v));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static Supplier<List<ProxyEntry>> supplierForMinecraft() {
        if (!proxyMinecraft()) {
            return List::of;
        } else {
            return SocksUtils.supplier();
        }
    }

    public static boolean proxyMinecraft() {
        try {
            return (Boolean) ConfigUtils.getEntryField(ProxyConfig.class, "proxyMinecraft").getValue();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
