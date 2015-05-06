package com.fzb.test;

import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.Router;
import com.fzb.http.server.impl.RouterServer;

public class ServerRun extends RouterServer{
	@Override
	public void configServer() {
		//config router
		Router.getInstance().addMapper("/user", MySimpleController.class);
		
		//config Intercepor
		InterceptorHelper.getInstance().addIntercepor(MyIntercepor.class);
	}
	
	public static void main(String[] args) {
		// 启动 server
		new Thread(new ServerRun()).start();
	}
}
