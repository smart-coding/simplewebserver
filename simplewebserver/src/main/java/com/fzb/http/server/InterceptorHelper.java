package com.fzb.http.server;

import com.fzb.http.kit.LoggerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterceptorHelper {

    private static final Logger LOGGER = LoggerUtil.getLogger(InterceptorHelper.class);
    private static InterceptorHelper instance = new InterceptorHelper();
    private List<Class<Interceptor>> interceptors = new ArrayList<Class<Interceptor>>();

    private InterceptorHelper() {

    }

    public static InterceptorHelper getInstance() {
        return instance;
    }

    public static void finish() {
        getInstance().addIntercepor(MethodInvokeInterceptor.class);
    }

    public static Interceptor getNextInterceptor(Class interceptor) {
        try {
            boolean flag = false;
            for (Class interceptor1 : getInstance().getInterceptors()) {
                if (flag) {
                    return (Interceptor) interceptor1.newInstance();
                }
                if (interceptor.getSimpleName().equals(interceptor1.getSimpleName())) {
                    flag = true;
                }
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Class<Interceptor>> getInterceptors() {
        return interceptors;
    }

    public void addIntercepor(Class interceptor) {
        try {
            if (interceptor.newInstance() instanceof Interceptor) {
                interceptors.add(interceptor);
            } else {
                LOGGER.log(Level.SEVERE,"the class " + interceptor.getCanonicalName() + " not implements Interceptor");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
