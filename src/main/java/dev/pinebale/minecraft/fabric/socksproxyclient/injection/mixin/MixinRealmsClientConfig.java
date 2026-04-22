package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.realmsclient.client.RealmsClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(RealmsClientConfig.class)
public class MixinRealmsClientConfig {
    @ModifyReturnValue(method = "getProxy", at = @At("RETURN"))
    private static Proxy modified(Proxy original) {
        LogUtils.logDebug("MixinRealmsClientConfig getProxy modified: Calling HttpUtils.getProxyObject");
        return HttpUtils.getProxyObject(ProxyMixinUtils.proxyRealmsApi());
    }
}
