package dev.pinebale.minecraft.fabric.socksproxyclient.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.ProxyEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("HttpUrlsUsage")
@Environment(EnvType.CLIENT)
public final class BaseConfig extends SocksProxyClientConfig {

    private static final BaseConfig INSTANCE;

    static {
        INSTANCE = new BaseConfig();
    }

    public static final String CATEGORY = "base";

    private static final SocksProxyClientConfigEntry<List<ProxyEntry>> proxies =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "proxies",
            new ArrayList<>(),
            ImmutableList::copyOf);
    private static final SocksProxyClientConfigEntry<List<String>> httpTestSubjects =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "httpTestSubjects",
            new ArrayList<>() {{
                add("https://ipinfo.io");
                add("http://connectivitycheck.gstatic.com/generate_204");
            }},
            ImmutableList::copyOf);
    private static final SocksProxyClientConfigEntry<List<String>> minecraftTestSubjects =
        new SocksProxyClientConfigEntry<>(INSTANCE.getClass(), "minecraftTestSubjects",
            new ArrayList<>() {{
                add("play.cubecraft.net");
            }},
            ImmutableList::copyOf);

    public BaseConfig() {
        super(CATEGORY + ".json");
    }

    @Override
    public JsonObject defaultEntries() {
        JsonObject obj = new JsonObject();

        {
            JsonArray proxyJsonArray = new JsonArray();
            proxies.getDefaultValue().forEach(entry -> {
                JsonObject proxyObj = new JsonObject();
                proxyObj.addProperty("host", ((InetSocketAddress) entry.getProxy().address()).getHostString());
                proxyObj.addProperty("port", ((InetSocketAddress) entry.getProxy().address()).getPort());
                proxyObj.addProperty("username", entry.getSocksProxyCredential().username());
                proxyObj.addProperty("password", entry.getSocksProxyCredential().password());
                proxyJsonArray.add(proxyObj);
            });
            obj.add(proxies.getJsonEntry(), proxyJsonArray);
        }

        {
            JsonArray httpTestSubjectJsonArray = new JsonArray();
            httpTestSubjects.getDefaultValue().forEach(httpTestSubjectJsonArray::add);
            obj.add(httpTestSubjects.getJsonEntry(), httpTestSubjectJsonArray);
        }

        {
            JsonArray minecraftTestSubjectJsonArray = new JsonArray();
            minecraftTestSubjects.getDefaultValue().forEach(minecraftTestSubjectJsonArray::add);
            obj.add(minecraftTestSubjects.getJsonEntry(), minecraftTestSubjectJsonArray);
        }

        return obj;
    }

    @Override
    public void fromJsonObject(JsonObject entries) {
        {
            List<ProxyEntry> proxyEntryArrayList = new ArrayList<>();
            JsonArray array = (JsonArray) entries.get(proxies.getJsonEntry());

            String host;
            int port;
            String username;
            String password;

            for (JsonElement element : array) {
                JsonObject proxyObj = (JsonObject) element;
                host = proxyObj.get("host").getAsString();
                if (host == null) {
                    host = "localhost";
                }
                try {
                    port = proxyObj.get("port").getAsInt();
                } catch (Exception e) {
                    port = 1080;
                }
                try {
                    username = proxyObj.get("username").getAsString();
                } catch (Exception e) {
                    username = null;
                }
                try {
                    password = proxyObj.get("password").getAsString();
                } catch (Exception e) {
                    password = null;
                }

                proxyEntryArrayList.add(new ProxyEntry(new InetSocketAddress(host, port), username, password));
            }
            proxies.setValue(proxyEntryArrayList);
        }

        {
            List<String> httpTestSubjectArrayList = new ArrayList<>();
            JsonArray array = (JsonArray) entries.get(httpTestSubjects.getJsonEntry());
            for (JsonElement element : array) {
                httpTestSubjectArrayList.add(element.getAsString());
            }
            httpTestSubjects.setValue(httpTestSubjectArrayList);
        }

        {
            List<String> minecraftTestSubjectArrayList = new ArrayList<>();
            JsonArray array = (JsonArray) entries.get(minecraftTestSubjects.getJsonEntry());
            for (JsonElement element : array) {
                minecraftTestSubjectArrayList.add(element.getAsString());
            }
            minecraftTestSubjects.setValue(minecraftTestSubjectArrayList);
        }
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();

        {
            JsonArray proxyJsonArray = new JsonArray();
            proxies.getValue().forEach(entry -> {
                JsonObject proxyObj = new JsonObject();
                proxyObj.addProperty("host", ((InetSocketAddress) entry.getProxy().address()).getHostString());
                proxyObj.addProperty("port", ((InetSocketAddress) entry.getProxy().address()).getPort());
                proxyObj.addProperty("username", entry.getSocksProxyCredential().username());
                proxyObj.addProperty("password", entry.getSocksProxyCredential().password());
                proxyJsonArray.add(proxyObj);
            });
            obj.add(proxies.getJsonEntry(), proxyJsonArray);
        }

        {
            JsonArray httpTestSubjectJsonArray = new JsonArray();
            httpTestSubjects.getValue().forEach(httpTestSubjectJsonArray::add);
            obj.add(httpTestSubjects.getJsonEntry(), httpTestSubjectJsonArray);
        }

        {
            JsonArray minecraftTestSubjectJsonArray = new JsonArray();
            minecraftTestSubjects.getValue().forEach(minecraftTestSubjectJsonArray::add);
            obj.add(minecraftTestSubjects.getJsonEntry(), minecraftTestSubjectJsonArray);
        }

        return obj;
    }
}
