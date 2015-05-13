package com.fzb.http.server;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fzb.http.server.impl.HttpServer;

public class Controller extends HttpServer{
	
	private HttpRequest request;
	private HttpResponse response;
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response){
		this.request=request;
		this.response=response;
		// 在请求路径中存在了. 认为其为文件
		if(request.getUri().indexOf(".")!=-1){
			response.wirteFile(new File(new File(request.getRealPath()).getParentFile()+"/static/"+request.getUri()));
			return;
		}
		Method method=Router.getInstance().getMethod(request.getUri());
		if(method==null){
			if(request.getUri().endsWith("/")){
				response.renderHtml(request.getUri()+"index.html");
			}
			else{
				response.renderError(404);
			}
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
