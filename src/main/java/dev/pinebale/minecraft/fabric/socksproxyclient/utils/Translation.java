package dev.pinebale.minecraft.fabric.socksproxyclient.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Environment(EnvType.CLIENT)
@SuppressWarnings("FieldCanBeLocal")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Translation {
    private static Locale locale;
    private static ResourceBundle resourceBundle;

    public static void load() {
        LogUtils.logDebug("Loading ResourceBundle");
        locale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("messages", locale);
        LogUtils.logDebug("Locale.getDefault(): {}", locale);
    }

    public static String get(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            LogUtils.logError("Missing resource key: " + key, e);
            return key;
        }
    }
}
