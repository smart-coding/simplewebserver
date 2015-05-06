package com.fzb.http.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fzb.http.server.impl.SimpleServer;

public class InterceptorHelper {

	private static InterceptorHelper instance=new InterceptorHelper();
	private static final Logger log=Logger.getLogger(InterceptorHelper.class);

	private InterceptorHelper(){
		
	}
	
	private List<Class<Interceptor>> interceptors=new ArrayList<Class<Interceptor>>();

	public List<Class<Interceptor>> getInterceptors() {
		return interceptors;
	}
	
	public void addIntercepor(Class interceptor){
		try {
			if(interceptor.newInstance() instanceof Interceptor){
				interceptors.add(interceptor);
			}
			else{
				log.error("the class "+interceptor.getCanonicalName()+ " not implements Interceptor");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static InterceptorHelper getInstance() {
		return instance;
	}
}
