package com.fzb.http.kit;

public class StringsUtil {

    public static final String VERSIONSTR = "1.2";

    public static String getHtmlStrByStatusCode(int statusCode) {
        return "<html><head><title>" + statusCode + " " + StatusCodeKit.getStatusCode(statusCode) + "</title></head><body><center><h1>" + statusCode + " " + StatusCodeKit.getStatusCode(statusCode) +"</h1></center><hr><center>simpleWebServer</center></body></html>";
    }
}
