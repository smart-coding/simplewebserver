package com.fzb.demo.file;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.MethodInvokeInterceptor;
import com.fzb.http.server.SimpleServerConfig;
import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.impl.ResponseConfig;
import com.fzb.http.server.impl.ServerConfig;

public class HttpServerConfig extends SimpleServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.getRouter().addMapper("/_file", MySimpleController.class);
        serverConfig.addInterceptor(MethodInvokeInterceptor.class);
        try {
            FreeMarkerKit.init(PathKit.getRootPath() + "/templates");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverConfig;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return null;
    }

    @Override
    public ResponseConfig getResponseConfig() {
        ResponseConfig responseConfig = new ResponseConfig();
        responseConfig.setIsGzip(true);
        return responseConfig;
    }
}
