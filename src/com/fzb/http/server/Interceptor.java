package com.fzb.http.server;

public interface Interceptor {

	boolean doInterceptor(HttpRequest request,HttpResponse response);
}
