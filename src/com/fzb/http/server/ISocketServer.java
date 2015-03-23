package com.fzb.http.server;

import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface ISocketServer {

	void listener();
	void distory();
	void create();
	void dispose(SocketChannel socket, Selector selector);
}
