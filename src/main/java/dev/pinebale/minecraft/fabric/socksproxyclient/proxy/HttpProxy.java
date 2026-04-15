package dev.pinebale.minecraft.fabric.socksproxyclient.proxy;

import dev.pinebale.minecraft.fabric.socksproxyclient.BaseConstants;
import dev.pinebale.minecraft.fabric.socksproxyclient.config.entry.ProxyEntry;
import dev.pinebale.minecraft.fabric.socksproxyclient.dns.NettyResolver;
import dev.pinebale.minecraft.fabric.socksproxyclient.utils.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LoggingHandler;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"LoggingSimilarMessage", "CodeBlock2Expr", "unused", "deprecation"})
@Environment(EnvType.CLIENT)
@Getter
public final class HttpProxy {

    public static final HttpProxy INSTANCE;

    static {
        INSTANCE = new HttpProxy();
    }

    private final String host;
    private int port;
    private Channel channel;
    private boolean fired = false;

    public HttpProxy() {
        this(0);
    }

    public HttpProxy(int port) {
        this("localhost", port);
    }

    public HttpProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void fire() {
        fire(f -> {
        });
    }

    public void fire(final GenericFutureListener<Future<? super Void>> firedCallback) {
        if (fired) {
            return;
        }

        new Thread(() -> {
            EventLoopGroup acceptorGroup = new NioEventLoopGroup(1);
            EventLoopGroup clientGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                ChannelFuture future = b.group(acceptorGroup, clientGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                .addLast("http_aggre", new HttpObjectAggregator(262144000))
                                .addLast("http_req_dec", new HttpRequestDecoder())
                                .addLast("opentunnel", new HttpProxyClientInboundHandler());
                        }
                    })
                    .bind(host, port).sync();
                channel = future.addListener(f -> {
                    if (f.isSuccess()) {
                        port = ((InetSocketAddress) future.channel().localAddress()).getPort();
                        fired = true;
                        LogUtils.logInfo("Internal http proxy is listening on {}", ((InetSocketAddress) future.channel().localAddress()).getAddress().getHostAddress() + ":" + port);
                    }
                }).addListener(firedCallback).channel();
                channel = channel.closeFuture().sync().addListener(f -> {
                    LogUtils.logInfo("Shutting off internal http proxy.");
                }).channel();
            } catch (Exception e) {
                LogUtils.logError("Error starting internal http proxy!", e);
            } finally {
                cease();
                acceptorGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
                clientGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
            }
        }).start();
    }

    public void cease() {
        if (!fired) {
            return;
        }
        fired = false;
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    private static final class HttpProxyClientInboundHandler extends SimpleChannelInboundHandler<HttpRequest> {
        private Channel remote;
        private Channel client;

        private boolean connectMethod = false;
        private HttpVersion httpVersion;
        private String remoteHttpHost = null;
        private int remoteHttpPort = -1;

        private boolean parsed = false;

        @Override
        public void channelActive(@NonNull ChannelHandlerContext ctx) {
            this.client = ctx.channel();
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) {
            if (!parsed) {
                connectMethod = msg.method().equals(HttpMethod.CONNECT);
                httpVersion = msg.protocolVersion();

                int k;

                remoteHttpHost = Objects.requireNonNull(msg.headers().getAsString("host"));

                k = remoteHttpHost.indexOf(":");
                if (k == -1) {
                    if (connectMethod) {
                        remoteHttpPort = 443;
                    } else {
                        remoteHttpPort = 80;
                    }
                } else {
                    remoteHttpPort = Integer.parseInt(Objects.requireNonNull(remoteHttpHost.substring(k + 1)));
                    remoteHttpHost = remoteHttpHost.substring(0, k);
                }

                parsed = true;
                LogUtils.logDebug("connectMethod: {}, httpVersion: {}, remoteHttpHost: {}, remoteHttpPort: {}", connectMethod, httpVersion, remoteHttpHost, remoteHttpPort);
            }

            if (remote == null) {
                final List<ProxyEntry> entries = SocksUtils.supplier().get();

                Bootstrap b = new Bootstrap().group(client.eventLoop())
                    .channel(client.getClass())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(@NonNull SocketChannel channel) {
                            SocksUtils.applyProxyChain(InetSocketAddress.createUnresolved(remoteHttpHost, remoteHttpPort), channel.pipeline(), () -> entries);
                            if (entries.isEmpty()) {
                                channel.pipeline().addFirst(new ChannelDuplexHandler());
                            }
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

                b.resolver(new AddressResolverGroup<InetSocketAddress>() {
                    @Override
                    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor eventExecutor) {
                        return new NettyResolver(eventExecutor).asAddressResolver();
                    }
                });

                ChannelFuture future = b.connect(remoteHttpHost, remoteHttpPort);
                future.addListener(f -> {
                    if (f.isSuccess()) {
                        remote = future.channel();
                        if (connectMethod) {
                            client.pipeline().addLast("temp_http_res_enc", new HttpResponseEncoder());
                            client.writeAndFlush(
                                new DefaultHttpResponse(
                                    httpVersion,
                                    new HttpResponseStatus(BaseConstants.HTTP_OK, "Connection Established")
                                )
                            ).addListener(f0 -> {
                                if (f0.isSuccess()) {
                                    client.pipeline().remove("temp_http_res_enc");
                                    channelRemoval();
                                    channelTakeover();
                                    LogUtils.logInfo("Open tunnel to remote {}:{}", remoteHttpHost, remoteHttpPort);
                                }
                            }).addListeners(
                                ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                                ChannelFutureListener.CLOSE_ON_FAILURE
                            );
                        } else {
                            remote.pipeline().addLast("temp_http_req_enc", new HttpRequestEncoder());
                            remote.writeAndFlush(msg).addListener(f0 -> {
                                if (f0.isSuccess()) {
                                    remote.pipeline().remove("temp_http_req_enc");
                                    channelRemoval();
                                    channelTakeover();
                                    LogUtils.logInfo("Open tunnel to remote {}:{}", remoteHttpHost, remoteHttpPort);
                                }
                            }).addListeners(
                                ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                                ChannelFutureListener.CLOSE_ON_FAILURE
                            );
                        }
                    } else {
                        shutOffActiveChannel(client);
                    }
                }).addListeners(
                    ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                    ChannelFutureListener.CLOSE_ON_FAILURE
                );
                remote = future.channel();
                client = client.closeFuture().addListener(f -> {
                    if (f.isDone()) {
                        LogUtils.logInfo("Closing tunnel to remote {}:{}", remoteHttpHost, remoteHttpPort);
                        shutOffActiveChannel(remote);
                    }
                }).channel();
                remote = remote.closeFuture().addListener(f -> {
                    if (f.isDone()) {
                        LogUtils.logInfo("Tunnel to remote {}:{} closed.", remoteHttpHost, remoteHttpPort);
                        shutOffActiveChannel(client);
                    }
                }).channel();
            }
        }

        private void channelRemoval() {
            client.pipeline().remove("http_aggre");
            client.pipeline().remove("http_req_dec");
        }

        private void channelTakeover() {
            clientChannelTakeover();
            remoteChannelTakeover();
        }

        private void clientChannelTakeover() {
            client.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                @Override
                public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                    remote.writeAndFlush(msg.retain()).addListeners(
                        ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                        ChannelFutureListener.CLOSE_ON_FAILURE
                    );
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    LogUtils.logError("Error in client channel!", cause);
                    shutOffActiveChannel(ctx.channel());
                }
            });
        }

        private void remoteChannelTakeover() {
            remote.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                @Override
                public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                    client.writeAndFlush(msg.retain()).addListeners(
                        ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
                        ChannelFutureListener.CLOSE_ON_FAILURE
                    );
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    LogUtils.logError("Error in remote channel!", cause);
                    shutOffActiveChannel(ctx.channel());
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LogUtils.logError("Error running internal http proxy!", cause);
            shutOffActiveChannel(ctx.channel());
        }

        @Override
        public void channelInactive(@NonNull ChannelHandlerContext ctx) {
            shutOffActiveChannel(remote);
        }

        private void shutOffActiveChannel(Channel channel) {
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
