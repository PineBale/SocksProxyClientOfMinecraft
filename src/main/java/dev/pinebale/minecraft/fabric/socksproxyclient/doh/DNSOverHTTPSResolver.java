package dev.pinebale.minecraft.fabric.socksproxyclient.doh;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.DNSUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SocksProxyClientDNSResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.doh.config.DNSOverHTTPSConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.xbill.DNS.DohResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.time.Duration;
import java.util.List;

@NoArgsConstructor
@Environment(EnvType.CLIENT)
public final class DNSOverHTTPSResolver implements SocksProxyClientDNSResolver {
    @SuppressWarnings("unused")
    private static final String NAME = "DNS-Over-HTTPS";

    private static Cache<String, List<Record>> cache;

    private static void initCache() {
        if (cache != null) {
            return;
        }
        try {
            cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds((Integer) ConfigUtils.getEntryField(DNSOverHTTPSConfig.class, "cacheTTL").getValue())).build();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public List<Record> resolve(final String hostname, final int recordType) throws Exception {
        initCache();
        // A record or SRV record
        List<Record> res = null;
        if (recordType == Type.A || recordType == Type.SRV) {
            res = cache.getIfPresent(hostname);
        }
        if (res != null) {
            if (res.isEmpty()) {
                cache.invalidate(hostname);
            } else {
                LogUtils.logDebug("Cache hit: {}", hostname);
                return res;
            }
        }
        final DohResolver resolver = new DohResolver(DNSOverHTTPSUtils.getDohUrl(), 1, Duration.ofSeconds(2L));
        resolver.setUsePost(true);
        final Lookup lookup = new Lookup(hostname, recordType);
        lookup.setResolver(resolver);
        if (DNSUtils.shouldDismissSystemHosts()) {
            LogUtils.logDebug("DNSOverHTTPSResolver: shouldDismissSystemHosts true");
            lookup.setHostsFileParser(null);
        }
        res = DNSUtils.performLookup(lookup);
        if (!res.isEmpty()) {
            cache.put(hostname, res);
        }
        return res;
    }
}
