package dev.pinebale.minecraft.fabric.socksproxyclient.injection.mixin;

import dev.pinebale.minecraft.fabric.socksproxyclient.injection.BaseMixinPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.net.InetSocketAddress;

/**
 * This mixin is referred in {@link BaseMixinPlugin}.
 * Don't forget to check it there if renaming.
 */
@Mixin(ServerStatusPinger.class)
@Environment(EnvType.CLIENT)
public class MixinServerStatusPingerNoLegacyPing {
    /**
     * @author PineBale
     * @reason Legacy ping won't work properly without hacking Minecraft.
     */
    @Overwrite
    private void pingLegacyServer(final InetSocketAddress resolvedAddress, final ServerAddress rawAddress, final ServerData data, final EventLoopGroupHolder eventLoopGroupHolder) {
        // no op
    }
}
