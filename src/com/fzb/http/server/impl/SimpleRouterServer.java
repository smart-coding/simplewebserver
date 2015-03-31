package com.fzb.http.server.impl;

import com.fzb.http.server.Router;

public class SimpleRouterServer extends RouterServer{

	@Override
	public void configRouter() {
		//config router
		Router.getInstance().addMapper("/user", MySimpleController.class);
	}
	
	public static void main(String[] args) {
		// 启动 server
		new Thread(new SimpleRouterServer()).start();
	}
}
