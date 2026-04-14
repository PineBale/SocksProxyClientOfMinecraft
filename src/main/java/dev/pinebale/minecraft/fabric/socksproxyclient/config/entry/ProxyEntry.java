package dev.pinebale.minecraft.fabric.socksproxyclient.config.entry;

import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.SocksProxyCredential;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantIfStatement", "RedundantCast", "unused", "unchecked", "RedundantSuppression"})
@Environment(EnvType.CLIENT)
@Getter
public final class ProxyEntry {
    @NotNull
    private Proxy proxy;

    @NotNull
    @Setter
    private SocksProxyCredential socksProxyCredential;

    public ProxyEntry(@NonNull InetSocketAddress sa) {
        this(sa, null, null);
    }

    public ProxyEntry(InetSocketAddress sa, @Nullable String username, @Nullable String password) {
        this(sa, new SocksProxyCredential(username, password));
    }

    public ProxyEntry(InetSocketAddress sa, @NonNull SocksProxyCredential socksProxyCredential) {
        this.proxy = new Proxy(Proxy.Type.SOCKS, sa);
        this.socksProxyCredential = socksProxyCredential;
    }

    public void setProxy(@NonNull Proxy proxy) {
        this.proxy = proxy;
        Validate.isTrue(proxy.type().equals(Proxy.Type.SOCKS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProxyEntry entry)) {
            return false;
        }
        Proxy proxy0 = this.getProxy();
        Proxy proxy1 = entry.getProxy();
        if (!proxy0.type().equals(proxy1.type())) {
            return false;
        }

        InetSocketAddress sa0 = (InetSocketAddress) proxy0.address();
        InetSocketAddress sa1 = (InetSocketAddress) proxy1.address();

        if (!compare(sa0.getHostString(), sa1.getHostString())) {
            return false;
        }
        if (sa0.getPort() != sa1.getPort()) {
            return false;
        }

        SocksProxyCredential c0 = this.getSocksProxyCredential();
        SocksProxyCredential c1 = entry.getSocksProxyCredential();

        if (!compare(c0.username(), c1.username())) {
            return false;
        }
        if (!compare(c0.password(), c1.password())) {
            return false;
        }
        return true;
    }

    private boolean compare(String s1, String s2) {
        if (s1 != null && s2 != null) {
            if (!s1.equals(s2)) {
                return false;
            }
        } else if (!(s1 == null && s2 == null)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getProxy().hashCode();
        result = 31 * result + getSocksProxyCredential().hashCode();
        return result;
    }
}
