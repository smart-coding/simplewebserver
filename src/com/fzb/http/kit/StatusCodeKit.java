package com.fzb.http.kit;

import java.util.HashMap;
import java.util.Map;

public class StatusCodeKit {

	private static Map<String,String> map=new HashMap<String,String>();
	
	static{
		map.put("200", "OK");
		map.put("302", "Moved  Permanently");
		map.put("404", "Not Found");
	}
	
	public static String getStatusCode(Integer code){
		return map.get(code+"");
	}
}
