package com.fzb.http.server;

import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.server.impl.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServerBuilder {

    private static final Logger LOGGER = LoggerUtil.getLogger(WebServerBuilder.class);

    private RequestConfig requestConfig;

    private ResponseConfig responseConfig;

    private ServerConfig serverConfig;

    private WebServerBuilder(Builder builder) {
        this.serverConfig = builder.serverConfig;
        this.responseConfig = builder.responseConfig;
        this.requestConfig = builder.requestConfig;
    }

    public void start() {
        SimpleServer socketServer;
        if (serverConfig == null) {
            serverConfig = new ServerConfig();
        }
        if (serverConfig.isSsl()) {
            socketServer = new SimpleHttpsServer(serverConfig, requestConfig, responseConfig);
        } else {
            socketServer = new SimpleServer(serverConfig, requestConfig, responseConfig);
        }
        if (serverConfig.getPort() != 0) {
            try {
                socketServer.create(serverConfig.getPort());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "create server error", e);
            }
        } else {
            socketServer.create();
        }
        socketServer.listener();
    }

    public void startWithThread() {
        final WebServerBuilder builder = this;
        new Thread() {
            @Override
            public void run() {
                builder.start();
            }
        }.start();
    }

    public static class Builder {

        private RequestConfig requestConfig;

        private ResponseConfig responseConfig;

        private ServerConfig serverConfig;

        public Builder() {
        }

        public Builder requestConfig(RequestConfig requestConfig) {
            this.requestConfig = requestConfig;
            return this;
        }

        public Builder responseConfig(ResponseConfig responseConfig) {
            this.responseConfig = responseConfig;
            return this;
        }

        public Builder serverConfig(ServerConfig serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public Builder config(SimpleServerConfig config) {
            serverConfig(config.getServerConfig());
            responseConfig(config.getResponseConfig());
            requestConfig(config.getRequestConfig());
            return this;
        }

        public WebServerBuilder build() {
            return new WebServerBuilder(this);
        }
    }

}
