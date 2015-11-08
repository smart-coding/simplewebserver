package com.fzb.http.server;

import com.fzb.http.server.impl.HttpServer;

public class Controller extends HttpServer {

    protected HttpRequest request;
    protected HttpResponse response;

    public Controller() {

    }

    public Controller(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
