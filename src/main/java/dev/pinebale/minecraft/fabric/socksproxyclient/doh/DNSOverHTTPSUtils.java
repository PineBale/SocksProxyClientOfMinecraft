package dev.pinebale.minecraft.fabric.socksproxyclient.doh;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.config.DNSOverHTTPSConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Environment(EnvType.CLIENT)
public final class DNSOverHTTPSUtils {
    public static DNSOverHTTPSProvider getProvider() throws Exception {
        return (DNSOverHTTPSProvider) ConfigUtils.getEntryField(DNSOverHTTPSConfig.class, "dohProvider").getValue();
    }

    public static String getCustomUrl() throws Exception {
        return (String) ConfigUtils.getEntryField(DNSOverHTTPSConfig.class, "customDohProvider").getValue();
    }

    public static String getDohUrl() throws Exception {
        DNSOverHTTPSProvider v = getProvider();
        LogUtils.logDebug("DOH PROVIDER: {}", v.getDisplayName());
        if (v.equals(DNSOverHTTPSProvider.CUSTOM)) {
            return getCustomUrl();
        }
        return v.getUrl();
    }
}
