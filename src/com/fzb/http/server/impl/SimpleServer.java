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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.kit.StringsUtil;
import com.fzb.http.server.Controller;
import com.fzb.http.server.HttpRequestMethod;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.ISocketServer;
import com.fzb.http.server.Interceptor;
import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.codec.impl.HttpDecoder;

public class SimpleServer implements ISocketServer{
	
	private static final Logger log=Logger.getLogger(SimpleServer.class);
	
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ExecutorService service=Executors.newFixedThreadPool(10);
	private Map<Socket,HttpDecoder> decoderMap=new ConcurrentHashMap<Socket,HttpDecoder>();
	@Override
	public void listener() {
		if(selector==null){
			return;
		}
		log.info("simpler Server is Run versionStr -> "+StringsUtil.VERSIONSTR);
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
							dispose(channel, request,key);
						}
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
			serverChannel.socket().bind(new InetSocketAddress(ConfigKit.getServerPort()));
			serverChannel.configureBlocking(false); // 设置为非阻塞方式,如果为true
													// 那么就为传统的阻塞方式
			selector = Selector.open(); // 静态方法 实例化selector
			serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 注册
																	// OP_ACCEPT事件
			log.info("simpler Server listening on port-> "+ConfigKit.getServerPort());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose(final SocketChannel channel, final HttpDecoder request,final SelectionKey key) {
		final long beginTime=System.currentTimeMillis();
		try {
			if(!request.doDecode(channel) || channel.socket().isClosed()){
				return;
			}
		} catch (Exception e1) {
			key.cancel();
			return;
		} 
		Thread thread=new Thread(){
			@Override
			public void run() {
				HttpResponse response=null;
				try { 
					response = new SimpleHttpResponse(channel, request);
					HttpRequestMethod sim = new Controller();
					boolean flag=true;
					List<Class<Interceptor>> interceptors=InterceptorHelper.getInstance().getInterceptors();
					for (Class<Interceptor> interceptor : interceptors) {
						flag=interceptor.newInstance().doInterceptor(request, response);
						if(!flag){
							break;
						}
					}
					if(flag){
						sim.doGet(request, response);
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.error("dispose error"+ e.getMessage());
				} finally{
					decoderMap.remove(channel.socket());
					if(response!=null){
						// 渲染错误页面
						if(!channel.socket().isClosed()){
							response.renderError(404);
						}
					}
					key.cancel();
					log.info(request.getUrl()+ " "+(System.currentTimeMillis()-beginTime) + " ms");
				}
			}
		};
		//thread.start();
		service.execute(thread);
		
	}
}
