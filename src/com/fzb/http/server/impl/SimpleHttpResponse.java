package com.fzb.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.common.util.IOUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.kit.StatusCodeKit;
import com.fzb.http.kit.StringsUtil;
import com.fzb.http.mimetype.MimeTypeUtil;
import com.fzb.http.server.ChunkedOutputStream;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.cookie.Cookie;

import flexjson.JSONSerializer;

public class SimpleHttpResponse implements HttpResponse{

	
	private static String serverName="SIMPLEWEBSERVER/"+StringsUtil.VERSIONSTR;
	private SocketChannel channel;
	private Map<String,String> header=new HashMap<String,String>();
	private HttpRequest request;
	private List<Cookie> cookieList=new ArrayList<Cookie>();
	public SimpleHttpResponse(SocketChannel channel,HttpRequest request) throws IOException{
		this.channel=channel;
		this.request=request;
		Cookie[] cookies=request.getCookies();
		if(cookies!=null){
			for (Cookie cookie : cookies) {
				if(cookie.isCreate()){
					cookieList.add(cookie);
				}
			}
		}
	}

	@Override
	public OutputStream getWirter() {
		return null;
	}

	@Override
	public void setHeader(String key, String value) {
		header.put(key,value);
	}

	@Override
	public void wirteFile(File file) {
		String chatset="";
		/*if(file.toString().endsWith(".html")){
			chatset=";charset=UTF-8";
		}*/
		if(file.exists()){
			try {
				// getMimeType
				ByteArrayOutputStream fout=new ByteArrayOutputStream();
				if(file.isDirectory()){
					renderByErrorStatusCode(302);
					return;
				}
				String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
				if(header.get("Content-Type")==null){
					header.put("Content-Type", MimeTypeUtil.getMimeStrByExt(ext)+chatset);
				}
				fout.write(warpperData(200,IOUtil.getByteByInputStream(new FileInputStream(file))));
				send(fout);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			renderByErrorStatusCode(404);
		}
	}
	private void send(ByteArrayOutputStream fout){
		ByteBuffer buffer=ByteBuffer.allocate(fout.toByteArray().length);
		try {
			buffer.put(fout.toByteArray());
			buffer.flip();
			
			while (buffer.hasRemaining()) {
			    int len = channel.write(buffer);
			    if (len < 0) {
			        throw new EOFException();
			    }
			}
			channel.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	@Override
	public void renderJson(Object json) {
		try {
			String body=new JSONSerializer().deepSerialize(json);
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			header.put("Content-Type", MimeTypeUtil.getMimeStrByExt("json")+";charset=UTF-8");
			fout.write(warpperData(200,body.getBytes("UTF-8")));
			send(fout);
		} catch (FileNotFoundException e) {
			renderByErrorStatusCode(404);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private byte[] warpperData(Integer statusCode,byte[] data) throws IOException{
		header.put("server", serverName);
		if(data.length>0){
			//header.put("Transfer-Encoding", "chunked");
			header.put("Content-Length", data.length+"");
		}
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		bout.write(("HTTP/1.1 "+statusCode+" "+StatusCodeKit.getStatusCode(statusCode)+"\r\n").getBytes());
		for (Entry<String, String> he : header.entrySet()) {
			bout.write(new String(he.getKey()+": "+he.getValue()+"\r\n").getBytes());
		}
		//deal cookie
		for (Cookie cookie : cookieList) {
			bout.write(new String("Set-Cookie: "+cookie+"\r\n").getBytes());
		}
		bout.write("\r\n".getBytes());
		if(data.length>0){
			//bout.write(warpperToChunkedByte(data));
			bout.write(data);
		}
		return bout.toByteArray();
	}
	
	private void renderByErrorStatusCode(Integer errorCode){
		if(errorCode==404){
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			try {
				header.put("Content-Type", "text/html;charset=UTF-8");
				fout.write(warpperData(404,StringsUtil.getNouFoundStr().getBytes()));
				send(fout);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(errorCode==302){
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			try {
				header.put("Location", "http://"+request.getHeader("Host")+"/"+request.getUrl()+"index.html");
				fout.write(warpperData(302,new String().getBytes()));
				send(fout);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void renderError(int errorCode) {
		renderByErrorStatusCode(errorCode);
	}

	@Override
	public void renderHtml(String urlPath) {
		wirteFile(new File(PathKit.getStaticPath()+urlPath));
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookieList.add(cookie);
	}

	@Override
	public void renderHtmlStr(String htmlContent) {
		try {
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			header.put("Content-Type", MimeTypeUtil.getMimeStrByExt("html")+";charset=UTF-8");
			fout.write(warpperData(200,htmlContent.getBytes("UTF-8")));
			send(fout);
		} catch (FileNotFoundException e) {
			renderByErrorStatusCode(404);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private byte[] warpperToChunkedByte(byte[] data){
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		System.out.println("before size "+data.length);
		ChunkedOutputStream out=new ChunkedOutputStream(bout);
		try {
			int block=128;
			int blockCount=data.length/block;
			for (int i=0;i<blockCount;i++) {
				out.write(HexaConversionUtil.subByts(data, i*block,block));
			}
			int last=data.length%block;
			if(last!=0){
				out.write(HexaConversionUtil.subByts(data, blockCount*block,last));
			}
			System.out.println("now size "+bout.toByteArray().length);
			out.close();
			return bout.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	@Override
	public void addHeader(String name, String value) {
		header.put(name, value);
	}
	
	@Override
	public void redirect(String url) {
		ByteArrayOutputStream fout=new ByteArrayOutputStream();
		header.put("Location", url);
		try {
			fout.write(warpperData(302,new byte[0]));
			send(fout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void forword(String url) {
		redirect("http://"+request.getHeader("Host")+"/"+url);
	}

	@Override
	public void renderFile(File file) {
		header.put("Content-Disposition", "attachment;filename="+file.getName());
		wirteFile(file);
	}
}
