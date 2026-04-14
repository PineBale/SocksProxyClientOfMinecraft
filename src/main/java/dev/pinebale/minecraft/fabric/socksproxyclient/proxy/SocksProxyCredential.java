package dev.pinebale.minecraft.fabric.socksproxyclient.proxy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public record SocksProxyCredential(@Nullable String username, @Nullable String password) {

    public SocksProxyCredential() {
        this(null, null);
    }

    public SocksProxyCredential(String username) {
        this(username, null);
    }
}
