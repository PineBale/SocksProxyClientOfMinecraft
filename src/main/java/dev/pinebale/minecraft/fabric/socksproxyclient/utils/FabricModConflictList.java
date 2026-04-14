package dev.pinebale.minecraft.fabric.socksproxyclient.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;

import java.util.Optional;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Environment(EnvType.CLIENT)
public final class FabricModConflictList {

    public static Supplier<Boolean> viafabricplus = new Supplier<>() {
        private Boolean v;

        @NonNull
        @Override
        public Boolean get() {
            if (v == null) {
                Optional<ModContainer> c = FabricLoader.getInstance().getModContainer("viafabricplus");
                if (c.isEmpty()) {
                    v = false;
                } else {
                    ModMetadata m = c.get().getMetadata();
                    LogUtils.logWarning("ViaFabricPlus {} detected", m.getVersion());
                    try {
                        v = VersionComparisonOperator.GREATER_EQUAL.test(m.getVersion(), Version.parse("3.0.0"));
                    } catch (VersionParsingException e) {
                        throw new Error(e);
                    }
                }
            }
            return v;
        }
    };
}
