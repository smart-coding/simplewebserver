package com.fzb.demo.file;

import java.util.List;

import com.fzb.http.server.Controller;


public class MySimpleController extends Controller {
	
	public void fetch(){
		List<SimpleFile> files= FilesManageUtil.getSimpleFileByPath(getRequest().getRealPath() + getRequest().getParaToStr("folder"), getRequest().getRealPath());
		getRequest().getAttr().put("files",files);
		getResponse().renderFreeMarker("/index");
	}
}
