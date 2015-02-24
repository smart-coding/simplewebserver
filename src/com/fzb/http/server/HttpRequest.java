package com.fzb.http.server;

import java.util.Map;

public interface HttpRequest {

	Map<String,String[]> getParamMap();
	String getHeader(String key);
	String getRomterAddr();
	String getUrl();
	String getRealPath();
	HttpMethod getMethod();
	
}
