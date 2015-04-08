package com.fzb.http.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public interface ISocketServer {

	void listener();
	void distory();
	void create();
	void dispose(SocketChannel socket, HttpRequest request,Iterator<SelectionKey> iter);
}
