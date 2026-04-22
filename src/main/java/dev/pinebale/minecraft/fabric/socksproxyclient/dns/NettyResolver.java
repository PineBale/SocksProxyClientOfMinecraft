package dev.pinebale.minecraft.fabric.socksproxyclient.dns;

import com.google.common.net.InetAddresses;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import io.netty.resolver.InetNameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
@Environment(EnvType.CLIENT)
public final class NettyResolver extends InetNameResolver {
    public NettyResolver(EventExecutor executor) {
        super(executor);
    }

    private List<InetAddress> getInetAddresses(final String hostname) throws Exception {
        LogUtils.logDebug("NettyResolver.getInetAddresses: hostname: {}", hostname);
        if (InetAddresses.isInetAddress(hostname)) {
            return List.of(InetAddress.getByName(hostname));
        }
        SocksProxyClientDNSResolver resolver = DNSUtils.createResolver();
        LogUtils.logInfo("{}: Resolving {}", DNSUtils.getResolverName(resolver.getClass()), hostname);
        // Because Minecraft server officially only supports IPv4, assume client has access to IPv4 Internet.
        List<Record> records = resolver.resolve(hostname, Type.A);
        InetAddress[] inets = new InetAddress[records.size()];
        for (int i = 0; i < records.size(); i++) {
            inets[i] = InetAddress.getByAddress(hostname, ((ARecord) records.get(i)).getAddress().getAddress());
        }
        return List.of(inets);
    }

    @Override
    protected void doResolve(String hostname, Promise<InetAddress> promise) throws Exception {
        try {
            promise.setSuccess(getInetAddresses(hostname).get(0));
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }

    @Override
    protected void doResolveAll(String hostname, Promise<List<InetAddress>> promise) throws Exception {
        try {
            promise.setSuccess(getInetAddresses(hostname));
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }
}
