package dev.pinebale.minecraft.fabric.socksproxyclient.injection.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;

@Environment(EnvType.CLIENT)
public interface IMixinLocalSampleLogger {
    void socksProxyClient$setPingingServerData(ServerData serverData);

    ServerData socksProxyClient$getPingingServerData();
}
