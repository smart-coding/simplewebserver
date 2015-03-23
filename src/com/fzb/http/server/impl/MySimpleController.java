package com.fzb.http.server.impl;

import com.fzb.http.server.Controller;


public class MySimpleController extends Controller {

	public void login(){
		getResponse().renderHtml("/index.html");
	}
	
}
