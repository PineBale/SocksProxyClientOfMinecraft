package dev.pinebale.minecraft.fabric.socksproxyclient.doh.injection.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xbill.DNS.DohResolver;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
@Mixin(DohResolver.class)
public class MixinDohResolver {
    @Inject(
        method = "lambda$getHttpClient$0",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/reflect/Method;invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 1,
            shift = At.Shift.BEFORE
        ),
        remap = false
    )
    private void injected(Executor key, CallbackInfoReturnable<Object> cir, @Local(name = "httpClientBuilder") Object httpClientBuilder) {
        HttpClient.Builder builder = (HttpClient.Builder) httpClientBuilder;
        builder.proxy(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                LogUtils.logDebug("MixinDohResolver lambda$getHttpClient$0 injected: Calling HttpUtils.getProxyObject");
                return List.of(HttpUtils.getProxyObject());
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

            }
        });
    }

    @Redirect(
        method = "sendAndGetMessageBytes",
        at = @At(value = "INVOKE", target = "Ljava/net/URL;openConnection()Ljava/net/URLConnection;"),
        remap = false
    )
    private URLConnection redirected(URL instance) throws IOException {
        LogUtils.logDebug("MixinDohResolver sendAndGetMessageBytes redirected: Calling HttpUtils.getProxyObject");
        return instance.openConnection(HttpUtils.getProxyObject());
    }
}
