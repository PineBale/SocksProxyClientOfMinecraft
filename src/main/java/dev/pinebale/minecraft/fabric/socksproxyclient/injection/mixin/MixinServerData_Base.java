package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Base;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ServerData.class)
public class MixinServerData_Base implements IMixinServerData_Base {
    @Unique
    private Consumer<ServerData> callbackOnPingFail;

    @Override
    public void socksProxyClient$setCallbackOnPingFail(Consumer<ServerData> callback) {
        this.callbackOnPingFail = callback;
    }

    @Override
    public Consumer<ServerData> socksProxyClient$getCallbackOnPingFail() {
        return callbackOnPingFail;
    }
}
