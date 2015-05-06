package com.fzb.http.server.impl;

import org.apache.log4j.Logger;

import com.fzb.http.server.Router;

public abstract class RouterServer extends SimpleServer implements Runnable {

	private static final Logger log=Logger.getLogger(SimpleServer.class);
	public abstract void configServer();

	@Override
	public void run() {
		configServer();
		log.info("routerMap: "+Router.getInstance().getRouterMap());
		create();
		listener();
	}
}
