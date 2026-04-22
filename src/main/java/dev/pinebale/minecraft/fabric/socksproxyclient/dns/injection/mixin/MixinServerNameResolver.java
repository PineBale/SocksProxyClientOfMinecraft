package dev.pinebale.minecraft.fabric.socksproxyclient.dns.injection.mixin;

import com.google.common.net.InetAddresses;
import dev.pinebale.minecraft.fabric.socksproxyclient.BaseConstants;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.DNSUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.SocksProxyClientDNSResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.multiplayer.resolver.ServerRedirectHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
@Environment(EnvType.CLIENT)
@Mixin(ServerNameResolver.class)
public class MixinServerNameResolver {
    @Shadow
    @Final
    @Mutable
    private ServerAddressResolver resolver;
    @Shadow
    @Final
    @Mutable
    private ServerRedirectHandler redirectHandler;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/resolver/ServerNameResolver;resolver:Lnet/minecraft/client/multiplayer/resolver/ServerAddressResolver;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void redirectResolver(ServerNameResolver instance, ServerAddressResolver value) {
        this.resolver = serverAddress -> {
            try {
                if (InetAddresses.isInetAddress(serverAddress.getHost())) {
                    LogUtils.logDebug("redirectResolver: {} is IP literal.", serverAddress.getHost());
                    return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(serverAddress.getHost(), serverAddress.getPort())));
                }
                LogUtils.logDebug("redirectResolver: {} is not IP literal. Going back to originalResolver", serverAddress.getHost());
                return originalResolver(serverAddress);
            } catch (Throwable e) {
                LogUtils.logError("Couldn't resolve {}", serverAddress.getHost(), e);
            }
            return Optional.empty();
        };
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/resolver/ServerNameResolver;redirectHandler:Lnet/minecraft/client/multiplayer/resolver/ServerRedirectHandler;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void redirectRedirectHandler(ServerNameResolver instance, ServerRedirectHandler value) {
        this.redirectHandler = serverAddress -> {
            if (serverAddress.getPort() == BaseConstants.DEFAULT_MINECRAFT_PORT) {
                try {
                    if (InetAddresses.isInetAddress(serverAddress.getHost())) {
                        LogUtils.logDebug("redirectRedirectHandler: {} is IP literal.", serverAddress.getHost());
                        return Optional.of(new ServerAddress(serverAddress.getHost(), serverAddress.getPort()));
                    }
                    LogUtils.logDebug("redirectRedirectHandler: {} is not IP literal. Going back to originalRedirectHandler", serverAddress.getHost());
                    return originalRedirectHandler(serverAddress);
                } catch (Throwable e) {
                    LogUtils.logWarning("Couldn't resolve _minecraft._tcp.{}: {}", serverAddress.getHost(), e.getMessage());
                }
            }
            return Optional.empty();
        };
    }

    @Unique
    private Optional<ResolvedServerAddress> originalResolver(ServerAddress serverAddress) throws Exception {
        List<Record> records = resolve(serverAddress.getHost(), Type.A);
        final ARecord arec = (ARecord) records.get(0);
        LogUtils.logDebug("originalResolver: Got ARecord: {}", arec);
        InetAddress inetAddress = InetAddress.getByAddress(serverAddress.getHost(), arec.getAddress().getAddress());
        LogUtils.logInfo("Successfully resolve {} to {}", serverAddress.getHost(), inetAddress.getHostAddress());
        return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(inetAddress, serverAddress.getPort())));
    }

    @Unique
    private Optional<ServerAddress> originalRedirectHandler(ServerAddress serverAddress) throws Exception {
        String addr0 = "_minecraft._tcp." + serverAddress.getHost();
        List<Record> records = resolve(addr0, Type.SRV);
        final SRVRecord srv = (SRVRecord) records.get(0);
        LogUtils.logDebug("originalRedirectHandler: Got SRVRecord: {}", srv);
        String host = srv.getTarget().toString(true);
        LogUtils.logInfo("Successfully resolve {} to {}:{}", addr0, host, srv.getPort());
        return Optional.of(new ServerAddress(host, srv.getPort()));
    }

    @Unique
    private List<Record> resolve(final String hostname, final int recordType) throws Exception {
        SocksProxyClientDNSResolver resolver = DNSUtils.createResolver();
        LogUtils.logInfo("{}: Resolving {}", DNSUtils.getResolverName(resolver.getClass()), hostname);
        return resolver.resolve(hostname, recordType);
    }
}
