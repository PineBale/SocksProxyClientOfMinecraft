package dev.pinebale.minecraft.fabric.socksproxyclient;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.pinebale.minecraft.fabric.socksproxyclient.screen.SettingsMenuScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SettingsMenuScreen::new;
    }
}
