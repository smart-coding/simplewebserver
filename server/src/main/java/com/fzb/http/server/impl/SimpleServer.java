package com.fzb.http.server.impl;

import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.kit.StringsUtil;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.Interceptor;
import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.codec.impl.HttpDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleServer implements ISocketServer {


    private static final Logger LOGGER = LoggerUtil.getLogger(SimpleServer.class);

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(10);
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
                    SocketChannel channel = null;
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
                                request = new HttpDecoder();
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
    public void distory() {

    }

    @Override
    public void create() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(PathKit
                    .getConfFile("/conf.properties"))));
            // server=new
            // ServerSocket(Integer.parseInt(prop.getProperty("server.port")));
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(ConfigKit.getServerPort()));
            serverChannel.configureBlocking(false); // 设置为非阻塞方式,如果为true
            // 那么就为传统的阻塞方式
            selector = Selector.open(); // 静态方法 实例化selector
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 注册
            // OP_ACCEPT事件
            LOGGER.info("simpler Server listening on port -> " + ConfigKit.getServerPort());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(final SocketChannel channel, final HttpDecoder request, final SelectionKey key) {
        final long beginTime = System.currentTimeMillis();
        try {
            if (!request.doDecode(channel) || channel.socket().isClosed()) {
                return;
            }
        } catch (Exception e1) {
            key.cancel();
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                HttpResponse response = null;
                try {
                    response = new SimpleHttpResponse(channel, request);
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
                    LOGGER.log(Level.SEVERE,"dispose error" + e.getMessage());
                } finally {
                    decoderMap.remove(channel.socket());
                    if (response != null) {
                        // 渲染错误页面
                        if (!channel.socket().isClosed()) {
                            response.renderError(404);
                        }
                    }
                    key.cancel();
                    LOGGER.info(request.getUri() + " " + (System.currentTimeMillis() - beginTime) + " ms");
                }
            }
        };
        //thread.start();
        service.execute(thread);

    }
}
