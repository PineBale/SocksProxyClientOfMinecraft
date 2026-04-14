package dev.pinebale.minecraft.fabric.socksproxyclient.config.entry;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SocksProxyClientInternalConfigEntry<T> extends SocksProxyClientConfigEntry<T> {
    public SocksProxyClientInternalConfigEntry(
        @NonNull Class<? extends SocksProxyClientConfig> configClass,
        @NonNull String jsonEntry,
        @NonNull T defaultValue,
        @NonNull Function<T, T> valueSupplier
    ) {
        super(configClass, jsonEntry, defaultValue, valueSupplier);
    }
}
