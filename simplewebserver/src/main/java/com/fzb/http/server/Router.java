package com.fzb.http.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router implements IRouter {

    private static Router instance = new Router();
    private Map<String, Method> routerMap = new HashMap<String, Method>();

    private Router() {

    }

    public static Router getInstance() {
        return instance;
    }

    @Override
    public void addMapper(String urlPath, Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getModifiers() == 1) {
                getRouterMap().put(urlPath + "/" + method.getName(), method);
            }
        }
        try {
            getRouterMap().put(urlPath, clazz.getClass().getMethod("index"));
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        } catch (SecurityException e) {
            //e.printStackTrace();
        }
    }

    public Method getMethod(String url) {
        return getRouterMap().get(url);
    }

    public Map<String, Method> getRouterMap() {
        return routerMap;
    }

    public void setRouterMap(Map<String, Method> routerMap) {
        this.routerMap = routerMap;
    }
}
