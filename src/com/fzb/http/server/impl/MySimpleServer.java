package com.fzb.http.server.impl;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;

public class MySimpleServer extends HttpServer {

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		/*System.out.println(request.getRomterAddr());
		System.out.println(request.getParamMap());*/
		System.out.println();
		Map<String,String[]> params=request.getParamMap();
		for (Entry<String, String[]> param : params.entrySet()) {
			System.out.println(param.getKey());
			String[] str=param.getValue();
			for (String string : str) {
				System.out.println(string);
			}
		}
		response.wirteFile(new File(new File(request.getRealPath()).getParentFile()+"/static/"+request.getUrl()));
	}
	
}
