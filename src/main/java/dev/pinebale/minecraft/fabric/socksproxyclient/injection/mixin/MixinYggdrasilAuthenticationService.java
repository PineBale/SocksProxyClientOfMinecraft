package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(YggdrasilAuthenticationService.class)
public class MixinYggdrasilAuthenticationService extends MixinHttpAuthenticationService {
    @Override
    protected Proxy redirectedGet0(HttpAuthenticationService instance) {
        LogUtils.logDebug("MixinYggdrasilAuthenticationService redirectedGet0: Calling HttpUtils.getProxyObject");
        return HttpUtils.getProxyObject(ProxyMixinUtils.proxyYggdrasil());
    }
}
