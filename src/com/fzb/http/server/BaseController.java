package com.fzb.http.server;

import com.fzb.http.server.Controller;

public class BaseController extends Controller{

	@Override
	public boolean before() {
		 return true;
	}

	@Override
	public boolean after() {
		 return true;
	}

}
