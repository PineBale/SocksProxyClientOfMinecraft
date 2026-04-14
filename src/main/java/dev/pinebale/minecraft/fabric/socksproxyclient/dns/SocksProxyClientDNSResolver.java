package dev.pinebale.minecraft.fabric.socksproxyclient.dns;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.xbill.DNS.Record;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface SocksProxyClientDNSResolver {
    List<Record> resolve(final String hostname, final int recordType) throws Exception;
}
