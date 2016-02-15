package com.fzb.http.server.execption;

/**
 * Created by xiaochun on 15-8-27.
 */
public class ContentToBigException extends RuntimeException{

    public ContentToBigException(String message) {
        super(message);
    }
}
