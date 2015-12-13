package com.fzb.http.server.codec;

public interface IHttpDeCoder {

    boolean doDecode(byte[] bytes) throws Exception;
}
