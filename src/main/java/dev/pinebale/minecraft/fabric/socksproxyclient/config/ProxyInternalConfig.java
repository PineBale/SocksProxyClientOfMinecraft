package dev.pinebale.minecraft.fabric.socksproxyclient.config;

import com.google.gson.JsonObject;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientInternalConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ProxyInternalConfig extends SocksProxyClientConfig {
    private static final ProxyInternalConfig INSTANCE;

    static {
        INSTANCE = new ProxyInternalConfig();
    }

    private static final SocksProxyClientInternalConfigEntry<Boolean> directConnectUseProxy =
        new SocksProxyClientInternalConfigEntry<>(INSTANCE.getClass(), "directConnectUseProxy", false, v -> v);

    public static final String CATEGORY = "proxy-internal";

    public ProxyInternalConfig() {
        super(CATEGORY + ".json");
    }

    @Override
    public JsonObject defaultEntries() {
        JsonObject obj = new JsonObject();
        obj.addProperty(directConnectUseProxy.getJsonEntry(), directConnectUseProxy.getDefaultValue());
        return obj;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(directConnectUseProxy.getJsonEntry(), directConnectUseProxy.getValue());
        return obj;
    }

    @Override
    public void fromJsonObject(JsonObject object) {
        directConnectUseProxy.setValue(object.get(directConnectUseProxy.getJsonEntry()).getAsBoolean());
    }
}
