package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.google.common.net.InetAddresses;
import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Proxy;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;

@Mixin(ServerData.class)
@Environment(EnvType.CLIENT)
public class MixinServerData_Proxy implements IMixinServerData_Proxy {
    @Shadow
    public String ip;

    @Unique
    private static final String SPC_KEY_USEPROXY = "socksproxyclient_useproxy";

    @Unique
    private boolean useProxy = true;  // true by default

    @Override
    public boolean socksProxyClient$isUseProxy() {
        return this.useProxy;
    }

    @Override
    public void socksProxyClient$setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void saveUseProxyKey(CallbackInfoReturnable<CompoundTag> cir, @Local(name = "tag") CompoundTag tag) {
        LogUtils.logDebug("ServerInfo toNbt: {}, {}: {}", ip, SPC_KEY_USEPROXY, useProxy);
        tag.putBoolean(SPC_KEY_USEPROXY, useProxy);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private static void loadUseProxyKey(final CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir, @Local(name = "server") ServerData server) {
        if (tag.tags.containsKey(SPC_KEY_USEPROXY)) {
            boolean entry;
            if (tag.tags.get(SPC_KEY_USEPROXY) instanceof NumericTag v) {
                entry = v.byteValue() != 0;
            } else {
                entry = false;
            }
            LogUtils.logDebug("ServerInfo fromNbt: {}, {}: {}", server.ip, SPC_KEY_USEPROXY, entry);
            ((IMixinServerData_Proxy) server).socksProxyClient$setUseProxy(entry);
        } else {
            try {
                InetAddress ip = InetAddresses.forString(server.ip);
                if (ip.isLoopbackAddress()) {
                    LogUtils.logDebug("ServerInfo fromNbt: {}, useProxy: {}", server.ip, false);
                    ((IMixinServerData_Proxy) server).socksProxyClient$setUseProxy(false);
                }
            } catch (Throwable e) {
                // NO-OP
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyUseProxyKey(final ServerData other, CallbackInfo ci) {
        final boolean entry = ((IMixinServerData_Proxy) other).socksProxyClient$isUseProxy();
        LogUtils.logDebug("ServerInfo copyFrom: {} to: {}, useProxy: {}", other.ip, this.ip, entry);
        this.socksProxyClient$setUseProxy(entry);
    }

}
