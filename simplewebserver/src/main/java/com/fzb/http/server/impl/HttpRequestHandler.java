package com.fzb.http.server.impl;

import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.server.Interceptor;
import com.fzb.http.server.codec.impl.HttpDecoder;
import com.fzb.http.server.handler.api.ReadWriteSelectorHandler;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequestHandler extends Thread {

    private static Logger LOGGER = LoggerUtil.getLogger(HttpRequestHandler.class);

    private SelectionKey key;
    private HttpDecoder request;
    private ServerConfig serverConfig;

    private SimpleHttpResponse response;
    private Socket socket;

    public HttpRequestHandler(SelectionKey key, ServerConfig serverConfig, ResponseConfig responseConfig) {
        this.key = key;

        Object[] objects = (Object[]) key.attachment();
        this.request = (HttpDecoder) objects[1];
        this.serverConfig = serverConfig;

        this.response = new SimpleHttpResponse((ReadWriteSelectorHandler) objects[0], request, responseConfig);
        this.socket = ((SocketChannel) key.channel()).socket();
    }

    @Override
    public void run() {

        //timeout
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (socket.isClosed()) {
                            break;
                        }
                        if (System.currentTimeMillis() - request.getCreateTime() > serverConfig.getTimeOut() * 1000) {
                            response.renderCode(504);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        try {
            List<Class<Interceptor>> interceptors = serverConfig.getInterceptors();
            for (Class<Interceptor> interceptor : interceptors) {
                if (!interceptor.newInstance().doInterceptor(request, response)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "dispose error " + e.getMessage());
        } finally {
            // 渲染错误页面
            if (!socket.isClosed()) {
                LOGGER.log(Level.WARNING, "forget close stream " + socket.toString());
                response.renderCode(404);
            }
            key.channel();
            LOGGER.info(request.getUri() + " " + (System.currentTimeMillis() - request.getCreateTime()) + " ms");
        }
    }
}
