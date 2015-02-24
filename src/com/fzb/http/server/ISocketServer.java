package com.fzb.http.server;

import java.net.Socket;

public interface ISocketServer {

	void dispose(Socket socket);
	void listener();
	void distory();
	void create();
}
