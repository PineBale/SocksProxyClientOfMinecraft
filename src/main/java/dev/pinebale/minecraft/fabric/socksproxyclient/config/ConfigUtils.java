package dev.pinebale.minecraft.fabric.socksproxyclient.config;

import com.google.common.net.HostAndPort;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;

import java.lang.reflect.Field;
import java.net.IDN;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "unused"})
@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtils {

    public static final Predicate<HostAndPort> hostAndPortValidity = (hostAndPort) -> {
        LogUtils.logDebug("hostAndPortValidity: {}", hostAndPort);
        String string = hostAndPort.getHost();
        int port = hostAndPort.getPort();
        if (!string.isEmpty() && port > 0 && port <= 65535) {
            IDN.toASCII(string);
            LogUtils.logDebug("hostAndPortValidity {} true", hostAndPort);
            return true;
        }
        LogUtils.logDebug("hostAndPortValidity {} false", hostAndPort);
        return false;
    };

    public static final Predicate<String> minecraftAddressValidity = (address) -> hostAndPortValidity.test(HostAndPort.fromString(address).withDefaultPort(SharedConstants.DEFAULT_MINECRAFT_PORT));

    public static final Predicate<String> addressValidity = (address) -> hostAndPortValidity.test(HostAndPort.fromString(address).withDefaultPort(0));

    public static <C extends SocksProxyClientConfig> C getConfigInstance(final Class<C> clazz) throws Exception {
        Field field = clazz.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        Object instance = field.get(null);
        if (instance == null) {
            field.set(null, clazz.getDeclaredConstructor().newInstance());
            instance = field.get(null);
        }
        Objects.requireNonNull(instance);
        if (!clazz.isInstance(instance)) {
            instance = null;
        }
        return (C) instance;
    }

    public static void loadAllConfig() throws Exception {
        ServiceLoader<SocksProxyClientConfig> serviceLoader = ServiceLoader.load(SocksProxyClientConfig.class, SocksProxyClientConfig.class.getClassLoader());
        for (Object provider : serviceLoader) {
            Class<? extends SocksProxyClientConfig> providerClass = provider.getClass().asSubclass(SocksProxyClientConfig.class);
            getConfigInstance(providerClass).load();
        }
    }

    public static void saveAllConfig() throws Exception {
        ServiceLoader<SocksProxyClientConfig> serviceLoader = ServiceLoader.load(SocksProxyClientConfig.class, SocksProxyClientConfig.class.getClassLoader());
        for (Object provider : serviceLoader) {
            Class<? extends SocksProxyClientConfig> providerClass = provider.getClass().asSubclass(SocksProxyClientConfig.class);
            getConfigInstance(providerClass).save();
        }
    }

    public static <T extends SocksProxyClientConfig> String getCategoryField(Class<T> clazz) throws Exception {
        String category;
        Field field = clazz.getDeclaredField("CATEGORY");
        field.setAccessible(true);
        category = (String) field.get(null);
        if (category == null) {
            category = clazz.getSimpleName();
        }
        return category;
    }

    public static SocksProxyClientConfigEntry<?> getEntryField(
        final Class<? extends SocksProxyClientConfig> configClass, final String fieldName) throws Exception {
        return getConfigInstance(configClass).getEntryField(fieldName);
    }

    public static <T> SocksProxyClientConfigEntry<T> getEntryField(
        final Class<? extends SocksProxyClientConfig> configClass, final String fieldName, final Class<T> valueType) throws Exception {
        return getConfigInstance(configClass).getEntryField(fieldName, valueType);
    }
}
