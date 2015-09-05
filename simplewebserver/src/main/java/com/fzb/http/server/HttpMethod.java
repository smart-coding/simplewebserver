package com.fzb.http.server;

public enum HttpMethod {

    PUT("PUT"), POST("POST"), GET("GET");
    private String method;

    private HttpMethod(String method) {
        this.method = method;
    }

    public String toString() {
        return method;
    }
}
