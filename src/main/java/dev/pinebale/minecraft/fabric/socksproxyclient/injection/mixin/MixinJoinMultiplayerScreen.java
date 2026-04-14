package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ProxyInternalConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Proxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(JoinMultiplayerScreen.class)
public class MixinJoinMultiplayerScreen {
    @Shadow
    private ServerData editingServer;

    @Shadow
    private ServerList servers;

    @Inject(method = "directJoinCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;join(Lnet/minecraft/client/multiplayer/ServerData;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void injected(final boolean result, CallbackInfo ci, @Local(name = "serverData") ServerData serverData) throws Exception {
        serverData.copyFrom(this.editingServer);
        this.servers.save();
        saveInternal();
    }

    @Inject(method = "directJoinCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", shift = At.Shift.BEFORE))
    private void injected(CallbackInfo ci) throws Exception {
        this.servers.save();
        saveInternal();
    }

    @Unique
    private void saveInternal() throws Exception {
        saveProxyInternal(((IMixinServerData_Proxy) this.editingServer).socksProxyClient$isUseProxy());
    }

    @Unique
    private void saveProxyInternal(boolean directConnectUseProxy) throws Exception {
        ProxyInternalConfig proxyInternalConfig = ConfigUtils.getConfigInstance(ProxyInternalConfig.class);
        proxyInternalConfig.getEntryField("directConnectUseProxy", Boolean.class).setValue(directConnectUseProxy);
        proxyInternalConfig.save();
    }
}
