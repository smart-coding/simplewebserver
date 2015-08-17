package com.fzb.demo.file;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.Router;
import com.fzb.http.server.impl.RouterServer;

public class ServerRun extends RouterServer {


	public ServerRun(String[] args) {
		super(args);
	}

	@Override
	public void configServer(String[] args) {
		//config router
		Router.getInstance().addMapper("/_file", MySimpleController.class);

		try {
			FreeMarkerKit.init(PathKit.getRootPath()+"/templates");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// 启动 server
		new Thread(new ServerRun(args)).start();
	}
}
