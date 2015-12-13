package com.fzb.http.server;

import com.fzb.http.server.cookie.Cookie;
import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.session.HttpSession;

import java.io.File;
import java.util.Map;

public interface HttpRequest {

    Map<String, String[]> getParamMap();

    String getHeader(String key);

    String getRemoteHost();

    String getUri();

    String getUrl();

    String getFullUrl();

    String getRealPath();

    String getQueryStr();

    HttpMethod getMethod();

    Cookie[] getCookies();

    HttpSession getSession();

    boolean getParaToBool(String key);

    String getParaToStr(String key);

    int getParaToInt(String key);

    File getFile(String key);

    Map<String, Object> getAttr();

    String getScheme();

    Map<String, String> getHeaderMap();

    byte[] getContentByte();

    RequestConfig getRequestConfig();
}
