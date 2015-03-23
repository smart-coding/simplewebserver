package com.fzb.http.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.fzb.http.kit.PathKit;
import com.fzb.http.server.HttpRequestMethod;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.codec.impl.HttpDecoder;

public class SimpleServer implements ISocketServer{

	private ServerSocketChannel serverChannel;
	private Selector selector;
	@Override
	public void listener() {
		HttpDecoder request=new HttpDecoder();
		while (true) {
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iter = keys.iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					SocketChannel channel = null;
					if (key.isAcceptable()){
						ServerSocketChannel server = (ServerSocketChannel)key.channel();
		                //获得客户端连接通道
	                    channel = server.accept();
	                    if(channel!=null){
	                    	channel.configureBlocking(false);
	                    	//在与客户端连接成功后，为客户端通道注册SelectionKey.OP_READ事件。
	                    	channel.register(selector, SelectionKey.OP_READ);
	                    }
					} 
					else if (key.isReadable()) {
						channel=(SocketChannel) key.channel();
						if(request.doDecode(channel)){
							HttpResponse response = new SimpleHttpResponse(channel, request);
							HttpRequestMethod sim = new MySimpleAction();
							sim.doGet(request, response);
							// 渲染错误页面
							if(!channel.socket().isClosed()){
								response.renderError(404);
							}
							request=new HttpDecoder();
						}
					}
				}
				iter.remove(); // 处理完事件的要从keys中删去
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void distory() {

	}

	@Override
	public void create() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(new File(PathKit
					.getConfFile("/conf.properties"))));
			// server=new
			// ServerSocket(Integer.parseInt(prop.getProperty("server.port")));
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(Integer.parseInt(prop.getProperty("server.port"))));
			serverChannel.configureBlocking(false); // 设置为非阻塞方式,如果为true
													// 那么就为传统的阻塞方式
			selector = Selector.open(); // 静态方法 实例化selector
			serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 注册
																	// OP_ACCEPT事件
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose(SocketChannel socket, Selector selector) {
		/*HttpRequest request;
		try {
			request = new SimpleHttpRequest();
			socket.register(selector, SelectionKey.OP_WRITE);
			HttpResponse response = new SimpleHttpResponse(socket, request);

			HttpRequestMethod sim = new UserServlet();
			sim.doGet(request, response);
			
			if(!socket.isClosed()){ response.renderError(404); }
			 
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	}

	public static void main(String[] args) {
		//run server
		SimpleServer server = new SimpleServer();
		server.create();

		server.listener();
	}

}
