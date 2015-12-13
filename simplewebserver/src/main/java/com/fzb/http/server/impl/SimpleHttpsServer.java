package com.fzb.http.server.impl;

import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.handler.api.ReadWriteSelectorHandler;
import com.fzb.http.server.handler.impl.SSLReadWriteSelectorHandler;
import com.fzb.http.server.ssl.SSLChannelFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SimpleHttpsServer extends SimpleServer {

    private SSLContext sslContext;

    public SimpleHttpsServer() {
        this(null, null, null);
    }

    public SimpleHttpsServer(ServerConfig serverConfig, RequestConfig requestConfig, ResponseConfig responseConfig) {
        super(serverConfig, requestConfig, responseConfig);
        String password = ConfigKit.get("server.ssl.keystore.password", "").toString();
        File file = new File(PathKit.getConfFile(ConfigKit.get("server.ssl.keystore", null).toString()));
        try {
            sslContext = SSLChannelFactory.getSSLContext(file, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ReadWriteSelectorHandler getReadWriteSelectorHandlerInstance(SocketChannel channel, SelectionKey key) throws IOException {
        return new SSLReadWriteSelectorHandler(channel, key, false, sslContext);
    }

    @Override
    public void create() {
        try {
            super.create(ConfigKit.getHttpsServerPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
