package com.fzb.http.server.impl;

import com.fzb.http.kit.*;
import com.fzb.http.server.HttpMethod;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.codec.IHttpDeCoder;
import com.fzb.http.server.execption.ContentLengthTooLargeException;
import com.fzb.http.server.handler.api.ReadWriteSelectorHandler;
import com.fzb.http.server.handler.impl.PlainReadWriteSelectorHandler;
import com.fzb.http.util.ServerInfo;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class SimpleServer implements ISocketServer {


    private static final Logger LOGGER = LoggerUtil.getLogger(SimpleServer.class);

    private Selector selector;
    private ServerConfig serverConfig;
    private RequestConfig requestConfig;
    private ResponseConfig responseConfig;
    private ServerContext serverContext = new ServerContext();

    public SimpleServer() {
        this(null, null, null);
    }

    public SimpleServer(ServerConfig serverConfig, RequestConfig requestConfig, ResponseConfig responseConfig) {
        if (requestConfig == null) {
            requestConfig = new RequestConfig();
        }
        if (serverConfig == null) {
            serverConfig = new ServerConfig();
            serverConfig.setDisableCookie(Boolean.valueOf(ConfigKit.get("server.disableCookie", requestConfig.isDisableCookie()).toString()));
        }
        if (responseConfig == null) {
            responseConfig = new ResponseConfig();
        }
        this.serverConfig = serverConfig;
        this.requestConfig = requestConfig;
        this.responseConfig = responseConfig;
        if (serverConfig.getTimeOut() == 0) {
            serverConfig.setTimeOut(Integer.parseInt(ConfigKit.get("server.timeout", 60).toString()));
        }
        if (serverConfig.getPort() == 0) {
            serverConfig.setPort(ConfigKit.getServerPort());
        }
        serverContext.setServerConfig(serverConfig);
    }

    public ReadWriteSelectorHandler getReadWriteSelectorHandlerInstance(SocketChannel channel, SelectionKey key) throws IOException {
        return new PlainReadWriteSelectorHandler(channel, key, false);
    }

    @Override
    public void listener() {
        if (selector == null) {
            return;
        }
        LOGGER.info("SimplerWebServer is run versionStr -> " + ServerInfo.getVersion());
        EnvKit.savePid(PathKit.getRootPath() + "/sim.pid");
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    SocketChannel channel;
                    if (!key.isValid() || !key.channel().isOpen()) {
                        continue;
                    } else if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        channel = server.accept();
                        if (channel != null) {
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        channel = (SocketChannel) key.channel();
                        if (channel != null && channel.isOpen()) {
                            IHttpDeCoder codec = serverContext.getHttpDeCoderMap().get(channel);
                            ReadWriteSelectorHandler handler = null;
                            try {
                                if (codec == null) {
                                    handler = getReadWriteSelectorHandlerInstance(channel, key);
                                    codec = new HttpDecoder(channel.getRemoteAddress(), getDefaultRequestConfig(), serverContext, handler);
                                    serverContext.getHttpDeCoderMap().put(channel, codec);
                                } else {
                                    handler = codec.getRequest().getHandler();
                                }
                                ByteBuffer byteBuffer = handler.handleRead();
                                byte[] bytes = HexConversionUtil.subBytes(byteBuffer.array(), 0, byteBuffer.array().length - byteBuffer.remaining());
                                // 数据完整时, 跳过当前循环等待下一个请求
                                if (!codec.doDecode(bytes)) {
                                    continue;
                                }
                                serverConfig.getExecutor().execute(new HttpRequestHandler(codec, key, serverConfig, getDefaultResponseConfig(), serverContext));
                                if (codec.getRequest().getMethod() != HttpMethod.CONNECT) {
                                    codec = new HttpDecoder(channel.getRemoteAddress(), getDefaultRequestConfig(), serverContext, handler);
                                    serverContext.getHttpDeCoderMap().put(channel, codec);
                                }
                            } catch (EOFException e) {
                                //do nothing
                                key.channel().close();
                                key.cancel();
                            } catch (ContentLengthTooLargeException e) {
                                if (handler != null && codec != null) {
                                    HttpResponse response = new SimpleHttpResponse(codec.getRequest(), getDefaultResponseConfig());
                                    response.renderCode(413);
                                }
                                key.channel().close();
                                key.cancel();
                            } catch (Exception e) {
                                if (handler != null && codec != null) {
                                    HttpResponse response = new SimpleHttpResponse(codec.getRequest(), getDefaultResponseConfig());
                                    response.renderCode(500);
                                }
                                key.channel().close();
                                key.cancel();
                            }
                        }
                    }
                    iter.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void create() {
        try {
            create(serverConfig.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(serverConfig.getHost(), port));
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        LOGGER.info("SimplerWebServer listening on port -> " + port);
    }

    private ResponseConfig getDefaultResponseConfig() {
        ResponseConfig config = new ResponseConfig();
        config.setCharSet("UTF-8");
        config.setIsGzip(responseConfig.isGzip());
        config.setDisableCookie(serverConfig.isDisableCookie());
        return config;
    }

    private RequestConfig getDefaultRequestConfig() {
        RequestConfig config = new RequestConfig();
        config.setDisableCookie(serverConfig.isDisableCookie());
        config.setRouter(serverConfig.getRouter());
        config.setIsSsl(serverConfig.isSsl());
        return config;
    }
}
