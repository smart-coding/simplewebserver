package com.fzb.demo.file;

import com.fzb.http.server.WebServerBuilder;

public class ServerRun {

    public static void main(String[] args) {
        new WebServerBuilder.Builder().config(new HttpServerConfig()).build().startWithThread();
        new WebServerBuilder.Builder().config(new HttpsServerConfig()).build().startWithThread();
    }
}
