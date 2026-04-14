package dev.pinebale.minecraft.fabric.socksproxyclient.injection.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;

import java.net.InetSocketAddress;

@Environment(EnvType.CLIENT)
/* For both Ping and Join */
public interface IMixinConnection {
    void socksProxyClient$setInetSocketAddress(InetSocketAddress inetSocketAddress);

    InetSocketAddress socksProxyClient$getInetSocketAddress();

    void socksProxyClient$setServerData(ServerData serverData);

    ServerData socksProxyClient$getServerData();
}
