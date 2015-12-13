package com.fzb.http.server.impl;

import com.fzb.http.server.Router;

public class RequestConfig {

    private boolean isSsl;
    private boolean disableCookie;
    private Router router;

    public boolean isDisableCookie() {
        return disableCookie;
    }

    protected void setDisableCookie(boolean disableCookie) {
        this.disableCookie = disableCookie;
    }

    public boolean isSsl() {
        return isSsl;
    }

    public void setIsSsl(boolean isSsl) {
        this.isSsl = isSsl;
    }

    public Router getRouter() {
        return router;
    }

    protected void setRouter(Router router) {
        this.router = router;
    }
}
