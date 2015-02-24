package com.fzb.http.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.fzb.http.server.HttpMethod;
import com.fzb.http.server.HttpRequest;

public class SimpleHttpRequest implements HttpRequest{
	
	private SocketAddress ipAddr;
	private Map<String,String> header=new HashMap<String,String>();
	private Map<String,String[]> paramMap;
	private String url;
	private HttpMethod method;
	
	public SimpleHttpRequest(Socket socket) throws IOException{
		this.ipAddr=socket.getRemoteSocketAddress();
		
		// parse HttpHeader
		InputStream in=socket.getInputStream();
		BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		StringBuilder sb=new StringBuilder();
		String tstr=null;
		while((tstr=bin.readLine())!=null && !"".equals(tstr)){
			sb.append(tstr+"\n");
		}
		String httpStr[]=new String(sb.toString()).split("\n");
		String pHeader=httpStr[0];
		if(!"".equals(pHeader.split(" ")[0])){
			try{
				method=HttpMethod.valueOf(pHeader.split(" ")[0]);
			}
			catch(IllegalArgumentException e){
				System.out.println("unsupport method " +pHeader.split(" ")[0]);
				return;
			}
			// 先得到请求头信息
			for(int i=1;i<httpStr.length;i++){
				header.put(httpStr[i].split(":")[0], httpStr[i].split(":")[1].substring(1));
			} 
			String paramStr=null;
			if(method==HttpMethod.GET){
				String turl=url=pHeader.split(" ")[1];
				if(turl.indexOf("?")!=-1){
					url=turl.substring(0,turl.indexOf("?"));
					paramStr=turl.substring(turl.indexOf("?")+1);
				}
			}
			else if(method==HttpMethod.POST){
				url=pHeader.split(" ")[1];
				char[] param=new char[Integer.parseInt(header.get("Content-Length"))];
				bin.read(param);
				paramStr=new String(param);
				//System.out.println(httpStr[i]);
			}
			if(paramStr!=null){
				String args[]=new String(paramStr).split("&");
				paramMap=new HashMap<String,String[]>();
				for (String string : args) {
					String kv[]=string.split("=");
					if(paramMap.get(kv[0])!=null){
						paramMap.get(kv[0])[paramMap.get(kv[0]).length]=kv[1];
					}
					else{
						String vs[]=new String[]{kv[1]};
						paramMap.put(kv[0], vs);
					}
				}
			}
 		}
		
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
		return url;
	}

	@Override
	public String getRealPath() {
		return this.getClass().getResource("/").getPath();
	}
	
}
