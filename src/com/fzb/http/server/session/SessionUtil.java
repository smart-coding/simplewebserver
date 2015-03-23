package com.fzb.http.server.session;

import java.util.HashMap;
import java.util.Map;

public class SessionUtil{

	
	public static Map<String,HttpSession> sessionMap=new HashMap<String,HttpSession>();
 
	public static HttpSession getSessionById(String sessionID){
		return sessionMap.get(sessionID);
	}
}
