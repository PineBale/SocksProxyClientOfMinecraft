package dev.pinebale.minecraft.fabric.socksproxyclient.utils;

import com.google.common.net.InetAddresses;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Environment(EnvType.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BaseUtils {
    @Getter
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public static boolean isIpInCidr(InetAddress ip, String cidr) {
        LogUtils.logDebug("isIpInCidr: ip {}, cidr {}", ip, cidr);
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddresses.forString(parts[0]);
            if (network instanceof Inet6Address) {
                return false;
            }
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = ip.getAddress();
            byte[] netBytes = network.getAddress();

            if (ipBytes.length != netBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (ipBytes[i] != netBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                return (ipBytes[fullBytes] & mask) == (netBytes[fullBytes] & mask);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
