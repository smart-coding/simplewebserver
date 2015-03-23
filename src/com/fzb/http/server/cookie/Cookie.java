package com.fzb.http.server.cookie;

import java.util.Date;

public class Cookie {
	
	private String name;
	private String value;
	private String domain;
	private String path;
	private Date outDate;
	private boolean create;
	
	public Cookie(boolean create){
		this.setCreate(create);
	}
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getDomain() {
		return domain;
	}


	public void setDomain(String domain) {
		this.domain = domain;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public Date getOutDate() {
		return outDate;
	}


	public void setOutDate(Date outDate) {
		this.outDate = outDate;
	}


	public Cookie(){
		//
	}
	
	
	
	@Override
	public String toString() {
		return name+"=" + value + ";"+"Path="+path+";HttpOnly";
	}


	public static Cookie[] saxToCookie(String cookieStr){
		String[] kv=cookieStr.split(";");
		Cookie[] cookies=new Cookie[1];
		
		Cookie cookie=new Cookie();
		cookie.setName(kv[0].split("=")[0]);
		cookie.setValue(kv[0].split("=")[1]);
		cookies[0]=cookie;
		return cookies;
	}

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}
}
