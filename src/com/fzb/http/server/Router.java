package com.fzb.http.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router implements IRouter {

	private static Map<String,Method> routerMap=new HashMap<String,Method>();
	private static Router instance=new Router();
	private Router(){
		
	}
	
	@Override
	public void addMapper(String urlPath, Class clazz) {
		Method[] methods= clazz.getDeclaredMethods();
		System.out.println(clazz);
		for (Method method : methods) {
			routerMap.put(urlPath+"/"+method.getName(), method);
		}
		try {
			routerMap.put(urlPath, clazz.getClass().getMethod("index"));
		} catch (NoSuchMethodException e) {
			//e.printStackTrace();
		} catch (SecurityException e) {
			//e.printStackTrace();
		}
		System.out.println(routerMap);
	}
	
	public static Router getInstance() {
		return instance;
	}
	
	public Method getMethod(String url){
		return routerMap.get(url);
	}
}
