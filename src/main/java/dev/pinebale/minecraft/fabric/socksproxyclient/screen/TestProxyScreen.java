package dev.pinebale.minecraft.fabric.socksproxyclient.screen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.pinebale.minecraft.fabric.socksproxyclient.BaseConstants;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.BaseConfig;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.ConfigUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Base;
import dev.pinebale.minecraft.fabric.socksproxyclient.injection.access.IMixinServerData_Proxy;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.HttpUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.ProxyUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.proxy.SocksUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.BaseUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.Translation;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public final class TestProxyScreen extends Screen {

    private final Screen parent;

    private Button runTestButton;

    private static final ServerStatusPinger pinger = new ServerStatusPinger();
    private static Long testTime = 0L;

    public TestProxyScreen(@NonNull Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.runTestButton = Button.builder(Component.literal(Translation.get("socksproxyclient.config.proxy.runTest")), _ -> this.doTest()).bounds(this.width / 2 - 100, 126, 200, 20).build();
        this.runTestButton.setTooltip(null);
        this.addRenderableWidget(this.runTestButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
            .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.updateRunTestButton();
    }

    private void doTest() {
        testTime = System.currentTimeMillis();
        try {
            List<String> httpSubjects = (List<String>) ConfigUtils.getEntryField(BaseConfig.class, "httpTestSubjects").getValue();
            List<String> minecraftSubjects = (List<String>) ConfigUtils.getEntryField(BaseConfig.class, "minecraftTestSubjects").getValue();
            for (String url : httpSubjects) {
                BaseUtils.getScheduler().submit(() -> testHttpUrl(url));
            }
            for (String address : minecraftSubjects) {
                BaseUtils.getScheduler().submit(() -> testMinecraftAddress(address));
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private void testMinecraftAddress(String address) {
        try {
            if (!ProxyUtils.proxyMinecraft()) {
                LogUtils.logDebug("Not running testing on {} because proxyMinecraft is false", address);
                return;
            }
            if (ProxyUtils.noProxyMinecraftFilter(address)) {
                LogUtils.logWarning("{} is in opt out list.", address);
                return;
            }

            showTestStart(address);

            ServerData entry = new ServerData(address, address, ServerData.Type.OTHER);
            ((IMixinServerData_Proxy) entry).socksProxyClient$setUseProxy(true);
            ((IMixinServerData_Base) entry).socksProxyClient$setCallbackOnPingFail((d) -> showTestResult(Pair.of(false, null), d.ip));

            pinger.pingServer(entry, () -> {}, () -> {
                showTestResult(Pair.of(true, null), entry.ip);
                LogUtils.logInfo("Pinged {}: Ping {}ms\n Version: {}\n Protocol version: {}\n Player count: {}",
                    entry.ip, entry.ping, entry.version.getString(), entry.protocol, entry.players.online());
            }, EventLoopGroupHolder.remote(minecraft.options.useNativeTransport()));

        } catch (Throwable e) {
            showTestResult(Pair.of(false, new RuntimeException("Failed to ping!", e)), address);
        }
    }

    private void testHttpUrl(String url) {
        showTestStart(url);
        final CompletableFuture<Pair<Boolean, Throwable>> test = testHttpFuture(url);
        showTestResult(test, url);
    }

    private void showTestStart(final String target) {
        LogUtils.logInfo("Testing connection to {}", target);
        this.minecraft.submit(() -> SystemToast.add(
            this.minecraft.getToastManager(),
            new SystemToast.SystemToastId(1000L),
            Component.literal(Translation.get("socksproxyclient.config.proxy.testing")),
            Component.literal(target)));
    }

    private CompletableFuture<Pair<Boolean, Throwable>> testHttpFuture(final String target) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = URI.create(target).toURL();
                final Proxy httpProxy = HttpUtils.getProxyObject(true);
                if (httpProxy.equals(Proxy.NO_PROXY)) {
                    LogUtils.logWarning("No proxy to test.");
                    return Pair.of(true, null);
                }

                final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(httpProxy);
                urlConnection.setConnectTimeout(com.mojang.authlib.minecraft.client.MinecraftClient.CONNECT_TIMEOUT_MS);
                urlConnection.setReadTimeout(com.mojang.authlib.minecraft.client.MinecraftClient.READ_TIMEOUT_MS);
                int res = urlConnection.getResponseCode();

                if (res != -1) {
                    if (res == BaseConstants.HTTP_OK || res == BaseConstants.HTTP_NO_CONTENT) {
                        LogUtils.logInfo("{} responded with {}", target, res);
                    } else {
                        LogUtils.logWarning("{} responded with {}", target, res);
                    }
                    if (res == BaseConstants.HTTP_OK) {
                        final InputStream inputStream = urlConnection.getInputStream();
                        final String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        final Gson gson = new Gson();
                        final JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                        LogUtils.logInfo("{} response: {}", target, jsonObject.toString());
                    }

                    urlConnection.disconnect();
                } else {
                    LogUtils.logWarning("{} is not responding.", target);
                }
            } catch (JsonSyntaxException e) {
                return Pair.of(true, new RuntimeException(target + " sent back no json.", e));
            } catch (IOException e) {
                return Pair.of(false, new RuntimeException("IO failure!!", e));
            }
            return Pair.of(true, null);
        });
    }

    private void showTestResult(final Pair<Boolean, Throwable> res, final String target) {
        this.minecraft.submit(() -> SystemToast.add(
            this.minecraft.getToastManager(),
            new SystemToast.SystemToastId(3000L),
            Component.literal(Translation.get(res.getLeft() ? "socksproxyclient.config.proxy.test.success" : "socksproxyclient.config.proxy.test.failure")),
            Component.literal(target)));

        if (res.getLeft()) {
            return;
        }

        Throwable t = res.getRight();
        if (t != null && !(t instanceof JsonSyntaxException)) {
            LogUtils.logError("Test not successful.", t);
        }
    }

    private void showTestResult(final CompletableFuture<Pair<Boolean, Throwable>> test, final String target) {
        test.thenApplyAsync(v -> {
            showTestResult(v, target);
            return null;
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void updateRunTestButton() {
        if (SocksUtils.supplier().get().isEmpty()) {
            this.runTestButton.setTooltip(Tooltip.create(Component.literal(Translation.get("socksproxyclient.config.proxy.runTest.disabled.emptyProxyChain"))));
            this.runTestButton.active = false;
        } else if (System.currentTimeMillis() - testTime <= 5000L) {
            this.runTestButton.setTooltip(null);
            this.runTestButton.active = false;
        } else {
            this.runTestButton.setTooltip(null);
            this.runTestButton.active = true;
        }
    }

    @Override
    public void tick() {
        this.updateRunTestButton();
        pinger.tick();
    }

    @Override
    public void removed() {
        pinger.removeAll();
    }
}
