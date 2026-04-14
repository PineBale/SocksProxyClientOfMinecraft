package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(HttpUtil.class)
public class MixinHttpUtil {
    @ModifyArg(
        method = "downloadFile",
        at = @At(
            value = "INVOKE",
            target = "Ljava/net/URL;openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"
        )
    )
    private static Proxy redirectedGet(Proxy instance) {
        return HttpUtils.getProxyObject(ProxyMixinUtils.proxyAssetsDownload());
    }
}
