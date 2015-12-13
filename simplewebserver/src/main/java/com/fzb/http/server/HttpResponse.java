package com.fzb.http.server;

import com.fzb.http.server.cookie.Cookie;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface HttpResponse {

    void writeFile(File file);

    void renderHtml(String urlPath);

    void renderJson(Object json);

    void renderCode(int errorCode);

    void addCookie(Cookie cookie);

    void renderHtmlStr(String htmlContent);

    void addHeader(String name, String value);

    void redirect(String url);

    void forward(String url);

    void renderFile(File file);

    void renderFreeMarker(String name);

    void write(InputStream inputStream);

    void write(InputStream inputStream, int code);

}
