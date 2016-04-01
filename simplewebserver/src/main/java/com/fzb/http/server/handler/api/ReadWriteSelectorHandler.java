package com.fzb.http.server.handler.api;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ReadWriteSelectorHandler {
    void handleWrite(ByteBuffer byteBuffer) throws IOException;

    ByteBuffer handleRead() throws IOException;

    void close();

    ByteBuffer getByteBuffer();

    int currentReadSize();
}
