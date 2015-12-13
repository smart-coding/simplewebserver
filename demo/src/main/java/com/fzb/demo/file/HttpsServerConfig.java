package com.fzb.demo.file;

import com.fzb.http.kit.ConfigKit;
import com.fzb.http.server.impl.ServerConfig;

public class HttpsServerConfig extends HttpServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = super.getServerConfig();
        serverConfig.setIsSsl(true);
        serverConfig.setPort(ConfigKit.getHttpsServerPort());
        return serverConfig;
    }
}
