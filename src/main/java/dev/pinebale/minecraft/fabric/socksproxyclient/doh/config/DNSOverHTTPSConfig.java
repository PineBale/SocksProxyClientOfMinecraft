package dev.pinebale.minecraft.fabric.socksproxyclient.doh.config;

import com.google.gson.JsonObject;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.SocksProxyClientConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientInternalConfigEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.DNSOverHTTPSProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class DNSOverHTTPSConfig extends SocksProxyClientConfig {
    private static final DNSOverHTTPSConfig INSTANCE;

    static {
        INSTANCE = new DNSOverHTTPSConfig();
    }

    public static final String CATEGORY = "doh";

    public DNSOverHTTPSConfig() {
        super(CATEGORY + ".json");
    }

    private static final SocksProxyClientConfigEntry<DNSOverHTTPSProvider> dohProvider =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "dohProvider",
            DNSOverHTTPSProvider.CLOUDFLARE, v -> v);
    @SuppressWarnings("DataFlowIssue")
    private static final SocksProxyClientConfigEntry<String> customDohProvider =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "customDohProvider",
            DNSOverHTTPSProvider.CLOUDFLARE.getUrl(), v -> v);
    private static final SocksProxyClientInternalConfigEntry<Integer> cacheTTL =
        new SocksProxyClientInternalConfigEntry<>(INSTANCE.getClass(), "cacheTTL", 120, v -> v < 60 ? 60 : v);

    @Override
    public JsonObject defaultEntries() {
        JsonObject obj = new JsonObject();
        obj.addProperty(dohProvider.getJsonEntry(), dohProvider.getDefaultValue().name());
        obj.addProperty(customDohProvider.getJsonEntry(), customDohProvider.getDefaultValue());
        obj.addProperty(cacheTTL.getJsonEntry(), cacheTTL.getDefaultValue());
        return obj;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(dohProvider.getJsonEntry(), dohProvider.getValue().name());
        obj.addProperty(customDohProvider.getJsonEntry(), customDohProvider.getValue());
        obj.addProperty(cacheTTL.getJsonEntry(), cacheTTL.getValue());
        return obj;
    }

    @Override
    public void fromJsonObject(JsonObject object) {
        dohProvider.setValue(DNSOverHTTPSProvider.valueOf(object.get(dohProvider.getJsonEntry()).getAsString()));
        customDohProvider.setValue(object.get(customDohProvider.getJsonEntry()).getAsString());
        cacheTTL.setValue(object.get(cacheTTL.getJsonEntry()).getAsInt());
    }
}
