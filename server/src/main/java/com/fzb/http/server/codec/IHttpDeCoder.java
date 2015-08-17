package com.fzb.http.server.codec;

import java.nio.channels.SocketChannel;

public interface IHttpDeCoder {

    boolean doDecode(SocketChannel channel) throws Exception;
}
