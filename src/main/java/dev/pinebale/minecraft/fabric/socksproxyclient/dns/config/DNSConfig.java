package dev.pinebale.minecraft.fabric.socksproxyclient.dns.config;

import com.google.gson.JsonObject;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SocksProxyClientDNSResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SystemResolver;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class DNSConfig extends SocksProxyClientConfig {
    private static final DNSConfig INSTANCE;

    static {
        INSTANCE = new DNSConfig();
    }

    public static final String CATEGORY = "dns";

    public DNSConfig() {
        super(CATEGORY + ".json");
    }

    private static final SocksProxyClientConfigEntry<Class<? extends SocksProxyClientDNSResolver>> resolver =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "resolver",
            SystemResolver.class,
            v -> v);
    private static final SocksProxyClientConfigEntry<Boolean> shouldDismissSystemHosts =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "shouldDismissSystemHosts",
            false,
            v -> v);

    @Override
    public JsonObject defaultEntries() {
        JsonObject obj = new JsonObject();
        obj.addProperty(resolver.getJsonEntry(), resolver.getDefaultValue().getName());
        obj.addProperty(shouldDismissSystemHosts.getJsonEntry(), shouldDismissSystemHosts.getDefaultValue());
        return obj;
    }

    @Override
    public void fromJsonObject(JsonObject entries) {
        try {
            resolver.setValue(Class.forName(entries.get(resolver.getJsonEntry()).getAsString(), false, SocksProxyClientDNSResolver.class.getClassLoader()).asSubclass(SocksProxyClientDNSResolver.class));
        } catch (Throwable e) {
            throw new Error(e);
        }
        shouldDismissSystemHosts.setValue(entries.get(shouldDismissSystemHosts.getJsonEntry()).getAsBoolean());
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(resolver.getJsonEntry(), resolver.getValue().getName());
        obj.addProperty(shouldDismissSystemHosts.getJsonEntry(), shouldDismissSystemHosts.getValue());
        return obj;
    }
}
