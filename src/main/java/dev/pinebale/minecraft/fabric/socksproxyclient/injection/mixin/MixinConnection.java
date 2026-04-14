package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinConnection;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinLocalSampleLogger;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import io.netty.channel.ChannelFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;

@Environment(EnvType.CLIENT)
@Mixin(Connection.class)
public class MixinConnection implements IMixinConnection {
    @Unique
    private InetSocketAddress remote;

    @Unique
    private ServerData serverData;

    @Override
    public InetSocketAddress socksProxyClient$getInetSocketAddress() {
        return remote;
    }

    @Override
    public void socksProxyClient$setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.remote = inetSocketAddress;
    }

    @Override
    public ServerData socksProxyClient$getServerData() {
        return serverData;
    }

    @Override
    public void socksProxyClient$setServerData(ServerData serverData) {
        this.serverData = serverData;
    }

    @Inject(
        method = "connectToServer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setBandwidthLogger(Lnet/minecraft/util/debugchart/LocalSampleLogger;)V", shift = At.Shift.AFTER)
    )
    private static void injected(final InetSocketAddress address, final EventLoopGroupHolder eventLoopGroupHolder, final LocalSampleLogger bandwidthLogger, CallbackInfoReturnable<Connection> cir, @Local(name = "connection") Connection connection) {
        ((IMixinConnection) connection).socksProxyClient$setServerData(((IMixinLocalSampleLogger) bandwidthLogger).socksProxyClient$getPingingServerData());
        LogUtils.logDebug("Pinging remote Minecraft server {}", address);
    }

    @Inject(
        method = "connect",
        at = @At("HEAD")
    )
    private static void injected(final InetSocketAddress address, final EventLoopGroupHolder eventLoopGroupHolder, final Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
        ((IMixinConnection) connection).socksProxyClient$setInetSocketAddress(address);
        LogUtils.logDebug("Connecting to remote Minecraft server {}", address);
    }
}
