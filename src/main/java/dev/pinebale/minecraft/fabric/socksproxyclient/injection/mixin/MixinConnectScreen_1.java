package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinConnection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.screens.ConnectScreen$1")
public class MixinConnectScreen_1 {

    @Shadow
    @Final
    ServerData val$server;

    @Inject(method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;connect(Ljava/net/InetSocketAddress;Lnet/minecraft/server/network/EventLoopGroupHolder;Lnet/minecraft/network/Connection;)Lio/netty/channel/ChannelFuture;",
            shift = At.Shift.BEFORE
        )
    )
    private void injected(CallbackInfo ci, @Local Connection connection) {
        ((IMixinConnection) connection).socksProxyClient$setServerData(val$server);
    }
}
