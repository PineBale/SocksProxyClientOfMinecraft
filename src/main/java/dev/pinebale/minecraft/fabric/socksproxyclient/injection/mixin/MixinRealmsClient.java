package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.RealmsClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(RealmsClient.class)
public class MixinRealmsClient {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/client/RealmsClientConfig;setProxy(Ljava/net/Proxy;)V"))
    private void redirected(Proxy proxy) {
        RealmsClientConfig.setProxy(HttpUtils.getProxyObject(ProxyMixinUtils.proxyRealmsApi()));
    }
}
