package com.fzb.http.server.codec.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.UUID;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.common.util.IOUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.HttpMethod;
import com.fzb.http.server.codec.IHttpDeCoder;
import com.fzb.http.server.cookie.Cookie;
import com.fzb.http.server.impl.SimpleHttpRequest;
import com.fzb.http.server.session.HttpSession;
import com.fzb.http.server.session.SessionUtil;

public class HttpDecoder extends SimpleHttpRequest implements IHttpDeCoder {

	private static String split="\r\n\r\n";
	
	public HttpDecoder(){
		
	}
	@Override
	public boolean doDecode(SocketChannel channel) throws Exception{
		this.ipAddr=channel.socket().getRemoteSocketAddress();
		// parse HttpHeader
		ByteBuffer buffer1=ByteBuffer.allocate(1024*16);
		int length=channel.read(buffer1);
		if(length<0){
			return false;
		}
		byte[] date=HexaConversionUtil.subByts(buffer1.array(), 0, length);
		//InputStream in=channel.socket().getInputStream();
		String fullStr=new String(date);
		boolean flag=false;
		if(dataBuffer==null && new String(date).contains("\r\n\r\n")){
			String tstr=new String(date).substring(0,new String(date).indexOf("\r\n\r\n"));
			String httpStr[]=new String(tstr).split("\r\n");
			String pHeader=httpStr[0];
			if(!"".equals(pHeader.split(" ")[0])){
				try{
					method=HttpMethod.valueOf(pHeader.split(" ")[0]);
				}
				catch(IllegalArgumentException e){
					System.out.println("unsupport method " +pHeader.split(" ")[0]);
					return false;
				}
				// 先得到请求头信息
				for(int i=1;i<httpStr.length;i++){
					header.put(httpStr[i].split(":")[0], httpStr[i].substring(httpStr[i].indexOf(":")+2));
				} 
				String paramStr=null;
				if(method==HttpMethod.GET){
					String turl=url=pHeader.split(" ")[1];
					if(turl.indexOf("?")!=-1){
						url=turl.substring(0,turl.indexOf("?"));
						paramStr=turl.substring(turl.indexOf("?")+1);
					}
					paramStrwapperToMap(paramStr);
					flag=true;
				}
				// 存在2种情况 
				// 1,POST 提交的数据一次性读取完成。
				// 2,POST 提交的数据一次性读取不完。
				else if(method==HttpMethod.POST){
					url=pHeader.split(" ")[1];
					Integer dateLength=Integer.parseInt(header.get("Content-Length"));
					//FIXME 无法分配过大的Buffer
					dataBuffer=ByteBuffer.allocate(dateLength);
					Integer remainLen=fullStr.indexOf(split)+split.getBytes().length;
					byte[] remain=HexaConversionUtil.subByts(date, remainLen, date.length-remainLen);
					dataBuffer.put(remain);
					flag=!dataBuffer.hasRemaining();
					if(flag){
						dealPostDate();
					}
				}
				// deal with cookie
				if(header.get("Cookie")!=null){
					cookies=Cookie.saxToCookie(header.get("Cookie").toString());
					if(Cookie.getJSessionId(header.get("Cookie").toString())==null){
						Cookie[] tcookies=new Cookie[cookies.length+1];
						for(int i=0;i<cookies.length;i++){
							tcookies[i]=cookies[i];
						}
						// new cookie
						String jsessionid=UUID.randomUUID().toString();
						Cookie cookie=new Cookie(true);
						cookie.setName(Cookie.JSESSIONID);
						cookie.setPath("/");
						cookie.setValue(jsessionid);
						tcookies[cookies.length]=cookie;
						cookies=tcookies;
						session=new HttpSession(jsessionid);
						SessionUtil.sessionMap.put(jsessionid, session);
					}
					else{
						session=SessionUtil.getSessionById(cookies[0].getValue());
					}
				}
				else{
					cookies=new Cookie[1];
					Cookie cookie=new Cookie(true);
					String jsessionid=UUID.randomUUID().toString();
					cookie.setName(Cookie.JSESSIONID);
					cookie.setPath("/");
					cookie.setValue(jsessionid);
					cookies[0]=cookie;
					session=new HttpSession(jsessionid);
					SessionUtil.sessionMap.put(cookies[0].getValue(), session);
				}
	 		}
		}
		else {
			dataBuffer.put(date);
			flag=!dataBuffer.hasRemaining();
			if(flag){
				dealPostDate();
			}
		}
		return flag;
	}
	
	public void paramStrwapperToMap(String paramStr){
		paramMap=new HashMap<String,String[]>();
		if(paramStr!=null){
			String args[]=new String(paramStr).split("&");
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
	
	private void dealPostDate(){
		String paramStr=null;
		if(header.get("Content-Type")!=null && header.get("Content-Type").toString().split(";")[0]!=null){
			if("multipart/form-data".equals(header.get("Content-Type").toString().split(";")[0])){
				//TODO 使用合理算法提高对网卡的利用率
				if(!dataBuffer.hasRemaining()){
					BufferedReader bin=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(dataBuffer.array())));
					//ByteArrayOutputStream bout=new ByteArrayOutputStream(d);
					String tstr=null;
					StringBuffer sb2=new StringBuffer();
					try {
						while((tstr=bin.readLine())!=null && !"".equals(tstr)){
							sb2.append(tstr+"\r\n");
							if(tstr.indexOf(":")!=-1){
								header.put(tstr.split(":")[0], tstr.substring(tstr.indexOf(":")+2));
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally{
						try {
							bin.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					
					String inputName=header.get("Content-Disposition").split(";")[1].split("=")[1].replace("\"", "");
					String fileName=header.get("Content-Disposition").split(";")[2].split("=")[1].replace("\"", "");
					File file=new File(PathKit.getRootPath()+"/temp/"+fileName);
					files.put(inputName, file);
					int length1=sb2.toString().split("\r\n")[0].getBytes().length+new String("\r\n").getBytes().length;
					int length2=sb2.toString().getBytes().length+2;
					int dataLength=Integer.parseInt(header.get("Content-Length"))-length1-length2-split.getBytes().length;
					IOUtil.writeBytesToFile(HexaConversionUtil.subByts(dataBuffer.array(), length2, dataLength), file);
					paramMap=new HashMap<String,String[]>();
					
				}
			}
			else{
				paramStr=new String(dataBuffer.array());
				paramStrwapperToMap(paramStr);
			}
		}
		else{
			paramStr=new String(dataBuffer.array());
			paramStrwapperToMap(paramStr);
		}
	}

}
