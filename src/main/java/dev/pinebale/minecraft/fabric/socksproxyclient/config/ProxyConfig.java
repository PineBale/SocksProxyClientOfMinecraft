package dev.pinebale.minecraft.fabric.socksproxyclient.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ProxyConfig extends SocksProxyClientConfig {

    private static final ProxyConfig INSTANCE;

    static {
        INSTANCE = new ProxyConfig();
    }

    private static final SocksProxyClientConfigEntry<Boolean> proxyMinecraft =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxyMinecraft",
            true, v -> v);
    private static final SocksProxyClientConfigEntry<List<String>> noProxyMinecraft =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "noProxyMinecraft",
            new ArrayList<>() {{
                add("localhost");
                add("127.0.0.0/8");
                add("192.168.0.0/16");
                add("172.16.0.0/12");
                add("10.0.0.0/8");
            }},
            ImmutableList::copyOf);

    private static final SocksProxyClientConfigEntry<Boolean> proxyYggdrasil =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxyYggdrasil",
            true, v -> v);
    private static final SocksProxyClientConfigEntry<Boolean> proxyRealmsApi =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxyRealmsApi",
            true, v -> v);
    private static final SocksProxyClientConfigEntry<Boolean> proxyAssetsDownload =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxyAssetsDownload",
            true, v -> v);
    private static final SocksProxyClientConfigEntry<Boolean> proxyBlockListSupplier =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxyBlockListSupplier",
            true, v -> v);

    public static final String CATEGORY = "proxy";

    public ProxyConfig() {
        super(CATEGORY + ".json");
    }

    @Override
    public JsonObject defaultEntries() {
        JsonObject obj = new JsonObject();
        obj.addProperty(proxyMinecraft.getJsonEntry(), proxyMinecraft.getDefaultValue());
        obj.addProperty(proxyYggdrasil.getJsonEntry(), proxyYggdrasil.getDefaultValue());
        obj.addProperty(proxyRealmsApi.getJsonEntry(), proxyRealmsApi.getDefaultValue());
        obj.addProperty(proxyAssetsDownload.getJsonEntry(), proxyAssetsDownload.getDefaultValue());
        obj.addProperty(proxyBlockListSupplier.getJsonEntry(), proxyBlockListSupplier.getDefaultValue());
        {
            JsonArray noProxyJsonArray = new JsonArray();
            noProxyMinecraft.getDefaultValue().forEach(noProxyJsonArray::add);
            obj.add(noProxyMinecraft.getJsonEntry(), noProxyJsonArray);
        }
        return obj;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(proxyMinecraft.getJsonEntry(), proxyMinecraft.getValue());
        obj.addProperty(proxyYggdrasil.getJsonEntry(), proxyYggdrasil.getValue());
        obj.addProperty(proxyRealmsApi.getJsonEntry(), proxyRealmsApi.getValue());
        obj.addProperty(proxyAssetsDownload.getJsonEntry(), proxyAssetsDownload.getValue());
        obj.addProperty(proxyBlockListSupplier.getJsonEntry(), proxyBlockListSupplier.getValue());
        {
            JsonArray noProxyJsonArray = new JsonArray();
            noProxyMinecraft.getValue().forEach(noProxyJsonArray::add);
            obj.add(noProxyMinecraft.getJsonEntry(), noProxyJsonArray);
        }
        return obj;
    }

    @Override
    public void fromJsonObject(JsonObject object) {
        proxyMinecraft.setValue(object.get(proxyMinecraft.getJsonEntry()).getAsBoolean());
        proxyYggdrasil.setValue(object.get(proxyYggdrasil.getJsonEntry()).getAsBoolean());
        proxyRealmsApi.setValue(object.get(proxyRealmsApi.getJsonEntry()).getAsBoolean());
        proxyAssetsDownload.setValue(object.get(proxyAssetsDownload.getJsonEntry()).getAsBoolean());
        proxyBlockListSupplier.setValue(object.get(proxyBlockListSupplier.getJsonEntry()).getAsBoolean());
        {
            List<String> noProxyArrayList = new ArrayList<>();
            JsonArray array = (JsonArray) object.get(noProxyMinecraft.getJsonEntry());
            for (JsonElement element : array) {
                noProxyArrayList.add(element.getAsString());
            }
            noProxyMinecraft.setValue(noProxyArrayList);
        }
    }
}
