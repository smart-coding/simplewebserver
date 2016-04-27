package com.fzb.http.server.codec;

import com.fzb.http.server.HttpRequest;

public interface IHttpDeCoder {

    boolean doDecode(byte[] bytes) throws Exception;

    HttpRequest getRequest();
}
