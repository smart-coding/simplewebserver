package com.fzb.http.kit;

import com.fzb.http.util.ServerInfo;

public class StringsUtil {

    public static String getHtmlStrByStatusCode(int statusCode) {
        return "<html><head><title>" + statusCode + " " + StatusCodeKit.getStatusCode(statusCode) + "</title></head><body><center><h1>" + statusCode + " " + StatusCodeKit.getStatusCode(statusCode) + "</h1></center><hr><center>" + ServerInfo.getName() + "/" + ServerInfo.getVersion() + "</center></body></html>";
    }
}
