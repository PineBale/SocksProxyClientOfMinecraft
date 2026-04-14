package dev.pinebale.minecraft.fabric.socksproxyclient.config.entry;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SocksProxyClientConfigEntry<T> {
    @Getter private final Class<? extends SocksProxyClientConfig> configClass;

    @Getter private final String jsonEntry;

    @Getter @NonNull private final T defaultValue;

    @Setter @NonNull private T value;

    @NonNull private final Function<T, T> valueSupplier;

    public @NonNull T getValue() {
        return valueSupplier.apply(value);
    }

    public SocksProxyClientConfigEntry(
        @NonNull Class<? extends SocksProxyClientConfig> configClass,
        @NonNull String jsonEntry,
        @NonNull T defaultValue,
        @NonNull Function<T, T> valueSupplier
    ) {
        this.configClass = configClass;
        this.jsonEntry = jsonEntry;
        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
        this.valueSupplier = valueSupplier;
    }

    public void reset() {
        this.value = this.defaultValue;
    }
}
