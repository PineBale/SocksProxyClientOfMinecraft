package dev.pinebale.minecraft.fabric.socksproxyclient.doh;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Getter
@Environment(EnvType.CLIENT)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DNSOverHTTPSProvider {
    CLOUDFLARE("Cloudflare", "https://1.0.0.1/dns-query"),
    GOOGLE("Google", "https://8.8.4.4/dns-query"),
    DNS_SB("dns.sb", "https://185.222.222.222/dns-query"),
    QUAD9("Quad9", "https://149.112.112.112/dns-query"),
    CISCO("Cisco OpenDNS/Cisco Umbrella", "https://208.67.220.222/dns-query"),
    ADGUARD("AdGuard DNS", "https://94.140.14.15/dns-query"),
    CUSTOM("Custom url", null);

    private final String displayName;
    @Nullable
    private final String url;
}
