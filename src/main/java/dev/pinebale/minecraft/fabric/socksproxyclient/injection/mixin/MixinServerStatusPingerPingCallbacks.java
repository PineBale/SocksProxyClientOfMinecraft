package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Base;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerStatusPinger.class)
@Environment(EnvType.CLIENT)
public class MixinServerStatusPingerPingCallbacks {
    @Inject(method = "onPingFailed", at = @At("HEAD"))
    private void injected(final Component reason, final ServerData data, CallbackInfo ci) {
        Consumer<ServerData> consumer = ((IMixinServerData_Base) data).socksProxyClient$getCallbackOnPingFail();
        if (consumer != null) {
            consumer.accept(data);
        }
    }
}
