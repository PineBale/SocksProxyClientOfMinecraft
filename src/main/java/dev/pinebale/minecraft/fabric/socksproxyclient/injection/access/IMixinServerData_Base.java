package dev.pinebale.minecraft.fabric.socksproxyclient.injection.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public interface IMixinServerData_Base {
    void socksProxyClient$setCallbackOnPingFail(Consumer<ServerData> callback);

    Consumer<ServerData> socksProxyClient$getCallbackOnPingFail();
}
