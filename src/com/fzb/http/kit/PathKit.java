package com.fzb.http.kit;

import java.io.File;

/**
 * 提供给一些路径供程序更方便的调用
 * @author Chun
 *
 */
public class PathKit {

	public static String getConfPath(){
		return getRootPath()+"/conf/";
	}
	
	public static String getRootPath(){
		return new File(PathKit.class.getClass().getResource("/").getPath()).getParentFile().toString();
	}
	
	public static String getConfFile(String file){
		return getConfPath()+file;
	}
	
	public static String getStaticPath(){
		return getRootPath()+"/static/";
	}
}
