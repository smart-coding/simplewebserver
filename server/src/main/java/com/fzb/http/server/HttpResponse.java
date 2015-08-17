package com.fzb.http.server;

import com.fzb.http.server.cookie.Cookie;

import java.io.File;
import java.io.OutputStream;

public interface HttpResponse {

    void wirteFile(File file);

    void renderHtml(String urlPath);

    void renderJson(Object json);

    OutputStream getWirter();

    void setHeader(String key, String value);

    void renderError(int errorCode);

    void addCookie(Cookie cookie);

    void renderHtmlStr(String htmlContent);

    void addHeader(String name, String value);

    void redirect(String url);

    void forword(String url);

    void renderFile(File file);

    void renderFreeMarker(String name);
}
