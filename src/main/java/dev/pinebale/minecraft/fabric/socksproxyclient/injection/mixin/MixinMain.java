package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.RevConstants;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpProxy;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Main.class)
public class MixinMain {
    @Inject(method = "main", at = @At(value = "HEAD"))
    private static void injected(String[] args, CallbackInfo ci) throws Exception {
        LogUtils.logInfo(String.format("Starting %s %s (Git: %s)", RevConstants.MOD_NAME, RevConstants.MOD_VERSION, RevConstants.MOD_DEV_VERSION));
        LogUtils.logInfo("Loading config");
        SocksProxyClientConfig.initRootPath(FabricLoader.getInstance().getConfigDir().resolve(RevConstants.MOD_ID));
        ConfigUtils.loadAllConfig();
        HttpProxy.INSTANCE.fire();
    }
}
