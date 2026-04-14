package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.mojang.patchy.MojangBlockListSupplier;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@Environment(EnvType.CLIENT)
@Mixin(MojangBlockListSupplier.class)
public class MixinMojangBlockListSupplier {
    @Redirect(
        method = "createBlockList",
        at = @At(value = "INVOKE", target = "Ljava/net/URL;openConnection()Ljava/net/URLConnection;"),
        remap = false
    )
    private URLConnection redirectedGet(URL instance) throws IOException {
        return instance.openConnection(HttpUtils.getProxyObject(ProxyMixinUtils.proxyBlockListSupplier()));
    }
}
