package com.fzb.http.server;

import java.io.File;
import java.util.Map;

import com.fzb.http.server.cookie.Cookie;
import com.fzb.http.server.session.HttpSession;

public interface HttpRequest {

	Map<String,String[]> getParamMap();
	String getHeader(String key);
	String getRomterAddr();
	String getUrl();
	String getRealPath();
	HttpMethod getMethod();
	Cookie[] getCookies();
	HttpSession getSession();
	boolean getParaToBool(String key);
	String getParaToStr(String key);
	int getParaToInt(String key);
	File getFile(String key);
}
