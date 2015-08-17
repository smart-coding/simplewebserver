package com.fzb.http.server;

import java.lang.reflect.Method;

public interface IRouter {
    Method getMethod(String url);

    void addMapper(String urlPath, Class clazz);
}
