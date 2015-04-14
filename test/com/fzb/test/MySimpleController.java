package com.fzb.test;

import java.util.List;

import com.fzb.http.server.BaseController;


public class MySimpleController extends BaseController {

	@Override
	public boolean before() {
		System.out.println("GGGGGGGGGG");
		return true;
	}
	
	public void fetch(){
		System.out.println(getRequest().getParaToStr("folder"));
		getResponse().renderHtmlStr(wapperHtml(getRequest().getRealPath()+getRequest().getParaToStr("folder"), getRequest().getRealPath()));
		/*Cookie cookie=new Cookie();
		cookie.setMaxAge(11111111);
		cookie.setName("xiaochun");
		cookie.setValue("111111111");
		getResponse().addCookie(cookie);
		System.out.println(getRequest().getHeader("User-Agent"));
		getResponse().renderHtml("/index.html");*/
	}
	
	public static String wapperHtml(String fodler,String root){
		List<SimpleFile> files=FilesManageUtil.getSimpleFileByPath(fodler, root);
		
		StringBuffer sb=new StringBuffer();
		String header="<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<link rel='stylesheet' rev='stylesheet' href='/style.css' type='text/css'/>\n<title></title>\n</head>\n<body>\n";
		
		sb.append(header);
		if(files.size()>0){
			for (SimpleFile simpleFile : files) {
				sb.append(simpleFile.getView()+"\n");
			}
		}
		else{
			sb.append("<span style='color:red'>Empty!</span>");
		}
		
		String footer="\n</body>\n</html>";
		sb.append(footer);
		return sb.toString();
	}
	
	
}
