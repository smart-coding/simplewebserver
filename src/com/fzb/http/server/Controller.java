package com.fzb.http.server;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fzb.http.server.impl.HttpServer;

public abstract class Controller extends HttpServer{
	
	private HttpRequest request;
	private HttpResponse response;
	public abstract boolean before();
	public abstract boolean after();
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response){
		this.request=request;
		this.response=response;
		if(before()){
			// 在请求路径中存在了. 认为其为文件
			if(request.getUrl().indexOf(".")!=-1){
				response.wirteFile(new File(new File(request.getRealPath()).getParentFile()+"/static/"+request.getUrl()));
				return;
			}
			Method method=Router.getInstance().getMethod(request.getUrl());
			if(method==null){
				response.renderError(404);
				return;
			}
			//
			try {
				Controller ctrl=(Controller) method.getDeclaringClass().newInstance();
				ctrl.request=request;
				ctrl.response=response;
				method.invoke(ctrl);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			after();
		}
		
	}
	
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		doPost(request, response);
	}

	public HttpResponse getResponse() {
		return response;
	}

	public HttpRequest getRequest() {
		return request;
	}
}
