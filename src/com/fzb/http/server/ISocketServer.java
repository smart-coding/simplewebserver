package com.fzb.http.server;

import java.nio.channels.SocketChannel;

import com.fzb.http.server.codec.impl.HttpDecoder;

public interface ISocketServer {

	void listener();
	void distory();
	void create();
	void dispose(final SocketChannel socket,final HttpDecoder request);
}
