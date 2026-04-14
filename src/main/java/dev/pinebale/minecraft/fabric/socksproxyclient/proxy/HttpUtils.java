package dev.pinebale.minecraft.fabric.socksproxyclient.proxy;

import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

    public static Proxy getProxyObject() {
        return getProxyObject(true);
    }

    public static Proxy getProxyObject(boolean useProxy) {
        LogUtils.logDebug("HttpUtils getProxyObject: {}", useProxy);
        if (!useProxy || !HttpProxy.INSTANCE.isFired()) {
            return Proxy.NO_PROXY;
        } else {
            return new Proxy(Proxy.Type.HTTP, HttpProxy.INSTANCE.getChannel().localAddress());
        }
    }
}
