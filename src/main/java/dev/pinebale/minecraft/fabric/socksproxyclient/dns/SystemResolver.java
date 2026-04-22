package dev.pinebale.minecraft.fabric.socksproxyclient.dns;

import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;

import java.time.Duration;
import java.util.List;

@NoArgsConstructor
@Environment(EnvType.CLIENT)
public final class SystemResolver implements SocksProxyClientDNSResolver {

    @SuppressWarnings("unused")
    private static final String NAME = "System";

    @Override
    public List<Record> resolve(final String hostname, final int recordType) throws Exception {
        final SimpleResolver resolver = new SimpleResolver();
        resolver.setTimeout(Duration.ofSeconds(2L));
        final Lookup lookup = new Lookup(hostname, recordType);
        lookup.setResolver(resolver);
        LogUtils.logDebug("SystemResolver calling DNSUtils.performLookup");
        return DNSUtils.performLookup(lookup);
    }
}
