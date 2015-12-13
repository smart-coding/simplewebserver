package com.fzb.http.server;

import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.impl.ResponseConfig;
import com.fzb.http.server.impl.ServerConfig;

public abstract class SimpleServerConfig {

    public abstract ServerConfig getServerConfig();

    public abstract RequestConfig getRequestConfig();

    public abstract ResponseConfig getResponseConfig();

}
