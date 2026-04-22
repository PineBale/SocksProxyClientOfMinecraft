package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.ProxyMixinUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.Proxy;

@Environment(EnvType.CLIENT)
@Mixin(SkinTextureDownloader.class)
public class MixinSkinTextureDownloader {
    @Redirect(method = "downloadSkin", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;proxy:Ljava/net/Proxy;", opcode = Opcodes.GETFIELD))
    private Proxy redirected(SkinTextureDownloader instance) {
        LogUtils.logDebug("MixinSkinTextureDownloader downloadSkin redirected: Calling HttpUtils.getProxyObject");
        return HttpUtils.getProxyObject(ProxyMixinUtils.proxyAssetsDownload());
    }
}
