package com.fzb.http.server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpRequestMethod;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;

public class SimpleServer implements ISocketServer{

	private ServerSocket server;
	
	@Override
	public void listener() {
		Socket socket=null;
		try {
			while((socket=server.accept())!=null){
				dispose(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void distory() {
		
	}

	@Override
	public void create() {
		try {
			server=new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void dispose(Socket socket) {
		HttpRequest request;
		try {
			request = new SimpleHttpRequest(socket);
			HttpResponse response=new SimpleHttpResponse(socket);
			
			HttpRequestMethod sim=new MySimpleServer();
			sim.doGet(request, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		SimpleServer server=new SimpleServer();
		server.create();
		
		server.listener();
	}

}
