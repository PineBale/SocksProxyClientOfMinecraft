package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.network.Connection$1")
public class MixinConnection_1 {
    @Shadow
    @Final
    Connection val$connection;

    @Inject(method = "initChannel", at = @At("TAIL"))
    private void injected(final Channel channel, CallbackInfo ci, @Local ChannelPipeline channelPipeline) {
        ProxyMixinUtils.determineProxySelection(val$connection, channelPipeline);
    }
}
