package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinLocalSampleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.Connection;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.net.InetSocketAddress;

@Mixin(ServerStatusPinger.class)
@Environment(EnvType.CLIENT)
public class MixinServerStatusPingerContextualInfo {
    @WrapOperation(method = "pingServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/util/debugchart/LocalSampleLogger;)Lnet/minecraft/network/Connection;"))
    private Connection setUseProxy(InetSocketAddress address, EventLoopGroupHolder eventLoopGroupHolder, LocalSampleLogger bandwidthLogger, Operation<Connection> original, @Local(argsOnly = true, name = "data") ServerData data) {
        if (bandwidthLogger == null) {
            bandwidthLogger = new LocalSampleLogger(1);
        }

        ((IMixinLocalSampleLogger) bandwidthLogger).socksProxyClient$setPingingServerData(data);
        return original.call(address, eventLoopGroupHolder, bandwidthLogger);
    }
}
