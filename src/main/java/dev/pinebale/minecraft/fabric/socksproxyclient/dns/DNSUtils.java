package dev.pinebale.minecraft.fabric.socksproxyclient.dns;

import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.config.DNSConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.List;

@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DNSUtils {

    @SuppressWarnings("unchecked")
    public static Class<? extends SocksProxyClientDNSResolver> getResolverClass() throws Exception {
        return (Class<? extends SocksProxyClientDNSResolver>) ConfigUtils.getEntryField(DNSConfig.class, "resolver").getValue();
    }

    public static boolean shouldDismissSystemHosts() throws Exception {
        return (Boolean) ConfigUtils.getEntryField(DNSConfig.class, "shouldDismissSystemHosts").getValue();
    }

    public static SocksProxyClientDNSResolver createResolver() {
        try {
            Class<? extends SocksProxyClientDNSResolver> v = getResolverClass();
            LogUtils.logDebug("Creating DNS resolver {}", v.getName());
            return v.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static String getResolverName(Class<? extends SocksProxyClientDNSResolver> clazz) throws Exception {
        Field field = clazz.getDeclaredField("NAME");
        field.setAccessible(true);
        String name = (String) field.get(null);
        if (name == null) {
            name = clazz.getName();
        }
        return name;
    }

    public static List<Record> performLookup(@NonNull Lookup lookup) throws UnknownHostException {
        lookup.setCache(null);
        int count = 0;
        while (count < 3) {
            LogUtils.logDebug("performLookup count: {}", count);
            lookup.run();
            while (true) {
                try {
                    lookup.getResult();
                    break;
                } catch (Throwable e) {
                    // no-op
                }
            }
            Record[] records = lookup.getAnswers();
            if (records == null || records.length == 0) {
                LogUtils.logDebug("No records found! {}: {}", lookup.getResult(), lookup.getErrorString());
                if (lookup.getResult() == Lookup.TRY_AGAIN) {
                    count++;
                    continue;
                }
                throw new UnknownHostException(lookup.getErrorString());
            } else {
                return List.of(records);
            }
        }
        throw new UnknownHostException("All 3 attempts failed");
    }
}
