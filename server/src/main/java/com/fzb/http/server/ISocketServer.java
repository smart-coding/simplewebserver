package com.fzb.http.server;

import com.fzb.http.server.codec.impl.HttpDecoder;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface ISocketServer {

    void listener();

    void distory();

    void create();

    void dispose(final SocketChannel socket, final HttpDecoder request, final SelectionKey key);
}
