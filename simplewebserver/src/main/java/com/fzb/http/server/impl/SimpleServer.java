package com.fzb.http.server.impl;

import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.StringsUtil;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.Interceptor;
import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.codec.impl.HttpDecoder;
import com.fzb.http.server.execption.ContentToBigException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleServer implements ISocketServer {


    private static final Logger LOGGER = LoggerUtil.getLogger(SimpleServer.class);

    private Selector selector;
    private int timeout;
    private boolean disableCookie;
    protected ExecutorService service = Executors.newFixedThreadPool(10);
    private Map<Socket, HttpDecoder> decoderMap = new ConcurrentHashMap<Socket, HttpDecoder>();

    @Override
    public void listener() {
        if (selector == null) {
            return;
        }
        LOGGER.info("simpler Server is Run versionStr -> " + StringsUtil.VERSIONSTR);
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    SocketChannel channel;
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        channel = server.accept();
                        if (channel != null) {
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        channel = (SocketChannel) key.channel();
                        if (channel != null) {
                            HttpDecoder request = decoderMap.get(channel.socket());
                            if (request == null) {
                                request = new HttpDecoder(disableCookie);
                                decoderMap.put(channel.socket(), request);
                            }
                            dispose(channel, request, key);
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
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(ConfigKit.getServerPort()));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("simpler Server listening on port -> " + ConfigKit.getServerPort());
            timeout = Integer.parseInt(ConfigKit.get("server.timeout", 60).toString());
            disableCookie = Boolean.valueOf(ConfigKit.get("server.disableCookie", false).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(final SocketChannel channel, final HttpDecoder request, final SelectionKey key) {
        final HttpResponse response = new SimpleHttpResponse(channel, request);
        try {
            if (!request.doDecode(channel) || channel.socket().isClosed()) {
                return;
            }
        } catch (Exception e) {
            if (e instanceof ContentToBigException) {
                response.renderCode(413);
            } else {
                response.renderCode(500);
            }
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep(1000);
                                    if (channel.socket().isClosed()) {
                                        break;
                                    }
                                    if (System.currentTimeMillis() - request.getCreateTime() > timeout * 1000) {
                                        response.renderCode(504);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    List<Class<Interceptor>> interceptors = InterceptorHelper.getInstance().getInterceptors();
                    for (Class<Interceptor> interceptor : interceptors) {
                        if (!interceptor.newInstance().doInterceptor(request, response)) {
                            break;
                        }
                    }
                    /*if(flag){
                        sim.doGet(request, response);
					}*/
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.log(Level.SEVERE, "dispose error" + e.getMessage());
                } finally {
                    decoderMap.remove(channel.socket());
                    // 渲染错误页面
                    if (!channel.socket().isClosed()) {
                        response.renderCode(404);
                    }
                    key.cancel();
                    LOGGER.info(request.getUri() + " " + (System.currentTimeMillis() - request.getCreateTime()) + " ms");
                }
            }
        };
        //thread.start();
        service.execute(thread);

    }
}
