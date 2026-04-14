package dev.pinebale.minecraft.fabric.socksproxyclient;

import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpProxy;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.BaseUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SocksProxyClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> BaseUtils.getScheduler().shutdownNow()));
        Runtime.getRuntime().addShutdownHook(new Thread(HttpProxy.INSTANCE::cease));
    }
}
