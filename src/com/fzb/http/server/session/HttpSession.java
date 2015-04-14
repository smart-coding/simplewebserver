package com.fzb.http.server.session;

import java.util.HashMap;
import java.util.Map;

public class HttpSession{

	private String sessionID;
	
	private Map<String,Object> attrMap=new HashMap<String,Object>();
	
	public HttpSession(String sessionID){
		this.sessionID=sessionID;
	}
	
	public void setAttr(String name,Object value){
		attrMap.put(name, value);
	}
	
	public static HttpSession getSessionById(String sessionID){
		return SessionUtil.sessionMap.get(sessionID);
	}

	public String getSessionId() {
		return sessionID;
	}

	public Object getAttr(String name) {
		return attrMap.get(name);
	}
	
	public void removeAttr(String name){
		attrMap.remove(name);
	}
}
