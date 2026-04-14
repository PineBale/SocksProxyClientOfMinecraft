package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinLocalSampleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LocalSampleLogger.class)
@Environment(EnvType.CLIENT)
public class MixinLocalSampleLogger implements IMixinLocalSampleLogger {
    @Unique
    private ServerData pingingServerData;

    @Override
    public void socksProxyClient$setPingingServerData(ServerData serverData) {
        this.pingingServerData = serverData;
    }

    @Override
    public ServerData socksProxyClient$getPingingServerData() {
        return pingingServerData;
    }
}
