package com.fzb.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fzb.common.util.IOUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.mimetype.MimeTypeUtil;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;

import flexjson.JSONSerializer;

public class SimpleHttpResponse implements HttpResponse{

	
	private static String serverName="SIMPLEWEBSERVER/1.1";
	private SocketChannel channel;
	private Map<String,String> header=new HashMap<String,String>();
	private HttpRequest request;
	public SimpleHttpResponse(SocketChannel channel,HttpRequest request) throws IOException{
		this.channel=channel;
		if(request.getCookies()[0].isCreate()){
			header.put("Set-Cookie",request.getCookies()[0].toString());
		}
		this.request=request;
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
		if(file.exists()){
			try {
				// getMimeType
				ByteArrayOutputStream fout=new ByteArrayOutputStream();
				
				if(file.isDirectory()){
					renderByErrorStatusCode(302);
					return;
				}
				fout.write("HTTP/1.1 200 OK\r\n".getBytes());
				String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
				header.put("Content-Type", MimeTypeUtil.getMimeStrByExt(ext)+";charset=UTF-8");
				fout.write(headerMapToStr());
				fout.write(IOUtil.getByteByInputStream(new FileInputStream(file)));
				//out.write(fout.toByteArray());
				//out.close();
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
		buffer.put(fout.toByteArray());
		try {
			buffer.flip();
			channel.write(buffer);
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void renderJson(Object json) {
		try {
			String body=new JSONSerializer().deepSerialize(json);
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			fout.write("HTTP/1.1 200 OK\r\n".getBytes());
			header.put("Content-Type", MimeTypeUtil.getMimeStrByExt("json")+";charset=UTF-8");
			fout.write(headerMapToStr());
			fout.write(body.getBytes());
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
	private byte[] headerMapToStr() throws IOException{
		header.put("server", serverName);
		ByteArrayOutputStream bout=new ByteArrayOutputStream();
		for (Entry<String, String> he : header.entrySet()) {
			bout.write(new String(he.getKey()+": "+he.getValue()+"\r\n").getBytes());
		}
		bout.write("\r\n".getBytes());
		return bout.toByteArray();
	}
	
	private void renderByErrorStatusCode(Integer errorCode){
		if(errorCode==404){
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			try {
				fout.write("HTTP/1.1 404 Not Found\r\n".getBytes());
				header.put("Content-Type", "text/html;charset=UTF-8");
				fout.write(headerMapToStr());
				fout.write("<html><body>404 file not found</body></html>".getBytes());
				send(fout);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(errorCode==302){
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			try {
				fout.write("HTTP/1.1 302 Moved  Permanently\r\n".getBytes());
				header.put("Location", "http://"+request.getHeader("HOST")+"/"+request.getUrl()+"index.html");
				fout.write(headerMapToStr());
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
	
}
