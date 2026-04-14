package dev.pinebale.minecraft.fabric.socksproxyclient.injection.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMixinServerData_Proxy {
    boolean socksProxyClient$isUseProxy();

    void socksProxyClient$setUseProxy(boolean useProxy);
}
