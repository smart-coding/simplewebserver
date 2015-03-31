package com.fzb.http.server.impl;

public abstract class RouterServer extends SimpleServer implements Runnable{

	public abstract void configRouter();
	
	@Override
	public void run() {
		configRouter();
		
		create();
		listener();
	}
}
