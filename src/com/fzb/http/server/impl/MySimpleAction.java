package com.fzb.http.server.impl;

import java.io.File;

import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;


public class MySimpleAction extends HttpServer {

	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		// 在请求路径中存在了. 认为其为文件
		if(request.getUrl().indexOf(".")!=-1){
			response.wirteFile(new File(new File(request.getRealPath()).getParentFile()+"/static/"+request.getUrl()));
			return;
		}
	}
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		doPost(request, response);
	}
	
}
