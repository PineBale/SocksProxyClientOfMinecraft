package dev.pinebale.minecraft.fabric.socksproxyclient.utils;

import dev.pinebale.minecraft.fabric.socksproxyclient.RevConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RevConstants.MOD_NAME);

    private static final String P = String.format("[%s] ", RevConstants.MOD_NAME);

    public static void logInfo(String message) {
        LOGGER.info("{}{}", P, message);
    }

    public static void logInfo(String format, Object... args) {
        LOGGER.info(P + format, args);
    }

    public static void logWarning(String message) {
        LOGGER.warn("{}{}", P, message);
    }

    public static void logWarning(String format, Object... args) {
        LOGGER.warn(P + format, args);
    }

    public static void logError(String format, Object... args) {
        LOGGER.error(P + format, args);
    }

    public static void logError(String message, Throwable t) {
        LOGGER.error(P + message, t);
    }

    public static void logError(String format, Object arg1, Object arg2) {
        LOGGER.error(P + format, arg1, arg2);
    }

    public static void logDebug(String format, Object... args) {
        LOGGER.debug(P + format, args);
    }
}
