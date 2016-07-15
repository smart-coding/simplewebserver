package com.fzb.http.server.impl;

import com.fzb.http.server.codec.IHttpDeCoder;

import java.nio.channels.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerContext {

    private ServerConfig serverConfig;

    private Map<Channel, IHttpDeCoder> httpDeCoderMap = new ConcurrentHashMap<>();

    public Map<Channel, IHttpDeCoder> getHttpDeCoderMap() {
        return httpDeCoderMap;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}
