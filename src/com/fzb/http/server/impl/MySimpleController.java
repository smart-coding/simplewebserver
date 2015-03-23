package com.fzb.http.server.impl;

import com.fzb.http.server.Controller;
import com.fzb.http.server.cookie.Cookie;


public class MySimpleController extends Controller {

	public void login(){
		Cookie cookie=new Cookie();
		cookie.setMaxAge(11111111);
		cookie.setName("xiaochun");
		cookie.setValue("111111111");
		getResponse().addCookie(cookie);
		System.out.println(getRequest().getHeader("User-Agent"));
		getResponse().renderHtml("/index.html");
	}
	
}
