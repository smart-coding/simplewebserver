package com.fzb.http.server;

import com.fzb.http.kit.LoggerUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 15-7-16.
 */
public class MethodInvokeInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerUtil.getLogger(MethodInvokeInterceptor.class);

    @Override
    public boolean doInterceptor(HttpRequest request, HttpResponse response) {
        // 在请求路径中存在了. 认为其为文件
        File file = new File(request.getRealPath() + request.getUri());
        if (file.exists() || request.getUri().contains(".")) {
            response.writeFile(file);
            return false;
        }
        Method method;
        Router router = request.getRequestConfig().getRouter();
        if (request.getUri().contains("-")) {
            method = router.getMethod(request.getUri().substring(0, request.getUri().indexOf("-")));
        } else {
            method = router.getMethod(request.getUri());
            if (method == null) {
                String index = request.getUri().substring(0, request.getUri().lastIndexOf("/") + 1) + "index";
                method = router.getMethod(index);
            }
        }
        if (method == null) {
            if (request.getUri().endsWith("/")) {
                response.renderHtml(request.getUri() + "index.html");
            } else {
                response.renderCode(404);
            }
            return false;
        }
        //
        try {
            Controller controller;
            try {
                Constructor constructor = method.getDeclaringClass().getConstructor(HttpRequest.class, HttpResponse.class);
                controller = (Controller) constructor.newInstance(request, response);
            } catch (NoSuchMethodException e) {
                controller = (Controller) method.getDeclaringClass().newInstance();
                controller.request = request;
                controller.response = response;
            }
            LOGGER.info("invoke method " + method);
            method.invoke(controller);
        } catch (Exception e) {
            response.renderCode(500);
            LOGGER.log(Level.SEVERE, "invoke error ", e);
            return false;
        }
        return true;
    }
}
