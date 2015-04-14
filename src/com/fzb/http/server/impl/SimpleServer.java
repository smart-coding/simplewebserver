package com.fzb.http.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fzb.http.kit.PathKit;
import com.fzb.http.server.BaseController;
import com.fzb.http.server.HttpRequestMethod;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.codec.impl.HttpDecoder;

public class SimpleServer implements ISocketServer{

	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ExecutorService service=Executors.newFixedThreadPool(10);
	private Map<Socket,HttpDecoder> decoderMap=new ConcurrentHashMap<Socket,HttpDecoder>();
	@Override
	public void listener() {
		if(selector==null){
			return;
		}
		while (true) {
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iter = keys.iterator();
				while (iter.hasNext()) {
					SelectionKey key= iter.next();
					SocketChannel channel = null;
					if (key.isAcceptable()){
						ServerSocketChannel server = (ServerSocketChannel)key.channel();
	                    channel = server.accept();
	                    if(channel!=null){
	                    	channel.configureBlocking(false);
	                    	channel.register(selector, SelectionKey.OP_READ);
	                    }
					} 
					else if (key.isReadable()) {
						channel=(SocketChannel) key.channel();
						if(channel!=null){
							HttpDecoder request=decoderMap.get(channel.socket());
							if(request==null){
								request=new HttpDecoder();
								decoderMap.put(channel.socket(), request);
							}
							dispose(channel, request);
						}
						key.cancel();
					}
					iter.remove();
				}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose(final SocketChannel channel, final HttpDecoder request) {
		final long beginTime=System.currentTimeMillis();
		try {
			if(!request.doDecode(channel) || channel.socket().isClosed()){
				System.out.println(decoderMap.size());
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread thread=new Thread(){
			@Override
			public void run() {
				HttpResponse response=null;
				try { 
					response = new SimpleHttpResponse(channel, request);
					HttpRequestMethod sim = new BaseController();
					sim.doGet(request, response);
				} catch (Exception e) {
					System.out.println(e.getLocalizedMessage());
					e.printStackTrace();
				} finally{
					decoderMap.remove(channel.socket());
					if(response!=null){
						// 渲染错误页面
						if(!channel.socket().isClosed()){
							response.renderError(404);
						}
					}
					System.out.println(request.getUrl()+ " "+(System.currentTimeMillis()-beginTime) + " ms");
				}
			}
		};
		//thread.start();
		service.execute(thread);
		
	}
}
