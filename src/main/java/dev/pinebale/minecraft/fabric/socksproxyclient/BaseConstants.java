package dev.pinebale.minecraft.fabric.socksproxyclient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseConstants {
    public static final int HTTP_OK = 200;
    public static final int HTTP_NO_CONTENT = 204;

    public static final int DEFAULT_MINECRAFT_PORT = 25565;
}
