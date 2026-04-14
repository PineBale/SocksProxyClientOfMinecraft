package dev.pinebale.minecraft.fabric.socksproxyclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.SocksProxyClientConfigEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.Getter;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "SameParameterValue", "SimplifiableIfStatement", "unused", "SizeReplaceableByIsEmpty", "SequencedCollectionMethodCanBeUsed"})
@Environment(EnvType.CLIENT)
public abstract class SocksProxyClientConfig {
    private static Path rootPath = null;

    public static void initRootPath(@NonNull Path path) {
        if (rootPath == null) {
            rootPath = path;
        } else {
            return;
        }
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    protected static Path configPathDir() {
        return rootPath;
    }

    @Getter
    private final File configFile;

    protected SocksProxyClientConfig(String filename) {
        this(configPathDir().resolve(filename).toFile());
    }

    protected SocksProxyClientConfig(File configFile) {
        this.configFile = configFile;
    }

    public abstract JsonObject defaultEntries();

    public abstract JsonObject toJsonObject();

    public abstract void fromJsonObject(JsonObject object);

    public void load() {
        LogUtils.logInfo("Reading config file {}", this.configFile.getName());
        if (!this.configFile.exists()) {
            writeJson(defaultEntries());
        }
        try {
            FileReader reader = readFile(this.configFile);
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(new JsonReader(reader), JsonObject.class);
            parseJson(object);
        } catch (Exception e) {
            LogUtils.logError("Error reading config file {}", this.configFile.getName(), e);
        }
    }

    public void save() {
        writeJson(toJsonObject());
    }

    private FileReader readFile(File file) throws IOException {
        return readFile(file, StandardCharsets.UTF_8);
    }

    private FileReader readFile(File file, Charset charset) throws IOException {
        return new FileReader(file, charset);
    }

    private FileWriter writeFile(File file, boolean append) throws IOException {
        return new FileWriter(file, append);
    }

    private FileWriter writeFile(File file, String content) throws IOException {
        return writeFile(file, content, false);
    }

    private FileWriter writeFile(File file, String content, boolean append) throws IOException {
        FileWriter writer = writeFile(file, append);
        writer.write(content);
        return writer;
    }

    protected void resetToDefault() {
        try {
            List<SocksProxyClientConfigEntry<?>> entries = entryFields();
            entries.forEach(SocksProxyClientConfigEntry::reset);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    private void parseJson(JsonObject object) {
        resetToDefault();
        final JsonObject defaults = defaultEntries();
        if (object == null || object.size() == 0) {
            writeJson(defaults);
            load();
            return;
        }
        LogUtils.logInfo("Parsing config json {}", this.configFile.getName());
        boolean reload = false;
        try {
            for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                if (!object.has(entry.getKey())) {
                    object.add(entry.getKey(), entry.getValue());
                    reload = true;
                }
            }
            if (reload) {
                writeJson(object);
                load();
                return;
            }
        } catch (Exception e) {
            LogUtils.logError("Error reading config json {}", this.configFile.getName(), e);
        }
        fromJsonObject(object);
    }

    private void writeJson(JsonObject entries) {
        try (FileWriter writer = writeFile(this.configFile, false)) {
            LogUtils.logInfo("Writing config to file {}", this.configFile.getName());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(entries));
        } catch (Exception e) {
            LogUtils.logError("Error writing config to file {}", this.configFile.getName(), e);
        }
    }

    protected Predicate<Field> entryVariablesListFilter =
        field -> SocksProxyClientConfigEntry.class.isAssignableFrom(field.getType());

    public List<SocksProxyClientConfigEntry<?>> entryFields(final Predicate<Field> listFilter) throws Exception {
        List<SocksProxyClientConfigEntry<?>> entries = new ArrayList<>();
        List<Field> fields = Arrays.stream(this.getClass().getDeclaredFields()).filter(listFilter).toList();
        for (Field field : fields) {
            field.setAccessible(true);
            SocksProxyClientConfigEntry<?> entry = (SocksProxyClientConfigEntry<?>) field.get(null);
            Class<?> clazz = entry.getDefaultValue().getClass();
            if (Integer.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz)
                || Enum.class.isAssignableFrom(clazz)
                || List.class.isAssignableFrom(clazz)
                || Class.class.isAssignableFrom(clazz)) {
                entries.add(entry);
            } else {
                throw new UnsupportedOperationException("Not using \"" + clazz.getName() + "\"!");
            }
        }
        return entries;
    }

    public SocksProxyClientConfigEntry<?> getEntryField(final String fieldName) throws Exception {
        return entryFields(field -> entryVariablesListFilter.test(field) && field.getName().equals(fieldName)).get(0);
    }

    public <T> SocksProxyClientConfigEntry<T> getEntryField(final String fieldName, final Class<T> valueType) throws Exception {
        return (SocksProxyClientConfigEntry<T>) entryFields(field -> {
                try {
                    field.setAccessible(true);
                    return entryVariablesListFilter.test(field)
                        && field.getName().equals(fieldName)
                        && valueType.isAssignableFrom(((SocksProxyClientConfigEntry<?>) field.get(null)).getDefaultValue().getClass());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        ).get(0);
    }

    public List<SocksProxyClientConfigEntry<?>> entryFields() throws Exception {
        return entryFields(field -> {
            try {
                field.setAccessible(true);
                return entryVariablesListFilter.test(field);
            } catch (Throwable t) {
                throw new Error(t);
            }
        });
    }
}
