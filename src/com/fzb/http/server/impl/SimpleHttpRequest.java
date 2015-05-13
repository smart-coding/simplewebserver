package com.fzb.http.server.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.fzb.http.kit.PathKit;
import com.fzb.http.server.HttpMethod;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.cookie.Cookie;
import com.fzb.http.server.session.HttpSession;

public class SimpleHttpRequest implements HttpRequest{
	
	protected SocketAddress ipAddr;
	protected Map<String,String> header=new HashMap<String,String>();
	protected Map<String,String[]> paramMap;
	protected String uri;
	protected String quertStr;
	protected HttpMethod method;
	protected Cookie[] cookies;
	protected HttpSession session;
	protected Map<String,File> files=new HashMap<String,File>();
	protected ByteBuffer dataBuffer;
	protected String scheme="http";
	
	public SimpleHttpRequest(){
	}

	@Override
	public Map<String, String[]> getParamMap() {
		return paramMap;
	}

	@Override
	public String getHeader(String key) {
		return header.get(key);
	}

	@Override
	public String getRomterAddr() {
		return ((InetSocketAddress)ipAddr).getHostString();
	}
	
	public HttpMethod getMethod(){
		return method;
	}

	@Override
	public String getUrl() {
		return scheme+"://"+header.get("Host")+uri;
	}

	@Override
	public String getRealPath() {
		return PathKit.getStaticPath();
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}



	@Override
	public String getParaToStr(String key) {
		if(paramMap.get(key)!=null){
			try {
				return URLDecoder.decode(paramMap.get(key)[0],"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public File getFile(String key) {
		return files.get(key);
	}

	@Override
	public int getParaToInt(String key) {
		if(paramMap.get(key)!=null){
			return Integer.parseInt(paramMap.get(key)[0]);
		}
		return 0;
	}

	@Override
	public boolean getParaToBool(String key) {
		if(paramMap.get(key)!=null){
			return paramMap.get(key)[0]=="on";
		}
		return false;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getFullUrl() {
		if(quertStr!=null){
			return getUrl()+"?"+quertStr;
		}
		return getUrl();
	}

	@Override
	public String getQueryStr() {
		return quertStr;
	}
	
}
