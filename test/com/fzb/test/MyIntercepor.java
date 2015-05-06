package com.fzb.test;

import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.Interceptor;

public class MyIntercepor implements Interceptor {

	@Override
	public boolean doInterceptor(HttpRequest request, HttpResponse response) {
		System.out.println("I'm a Interceptor, will do sth");
		return true;
	}
}
