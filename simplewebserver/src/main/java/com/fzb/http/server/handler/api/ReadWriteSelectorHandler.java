package com.fzb.http.server.handler.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

public interface ReadWriteSelectorHandler {
    void handleWrite(ByteBuffer byteBuffer) throws IOException;

    ByteBuffer handleRead() throws IOException;

    void close();

    ByteBuffer getByteBuffer();

    int currentReadSize();

    SocketChannel getChannel();
}
