package com.fzb.test;

import com.fzb.http.server.Router;
import com.fzb.http.server.impl.RouterServer;

public class ServerRun extends RouterServer{

	@Override
	public void configRouter() {
		//config router
		Router.getInstance().addMapper("/_file", MySimpleController.class);
	}
	
	public static void main(String[] args) {
		// 启动 server
		new Thread(new ServerRun()).start();
	}
}
