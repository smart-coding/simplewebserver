package com.fzb.http.server.impl;

import com.fzb.http.kit.EnvKit;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.Interceptor;
import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.Router;

import java.util.logging.Logger;

public abstract class RouterServer extends SimpleServer implements Runnable {

    private static final Logger LOGGER = LoggerUtil.getLogger(RouterServer.class);
    private String[] args;

    public abstract void configServer(String[] args);

    public RouterServer() {
    }

    public RouterServer(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        EnvKit.savePid(PathKit.getRootPath() + "/sim.pid");
        configServer(args);
        LOGGER.info("routerMap: " + Router.getInstance().getRouterMap());
        //default
        InterceptorHelper.finish();
        create();
        listener();

    }

}
