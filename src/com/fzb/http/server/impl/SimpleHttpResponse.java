package com.fzb.http.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fzb.common.util.IOUtil;
import com.fzb.http.mimetype.MimeTypeUtil;
import com.fzb.http.server.HttpResponse;

public class SimpleHttpResponse implements HttpResponse{

	private OutputStream out;
	private Map<String,String> header=new HashMap<String,String>();
	
	public SimpleHttpResponse(Socket socket) throws IOException{
		out=socket.getOutputStream();
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
		// getMimeType
		
		try {
			
			
			FileInputStream fin=new FileInputStream(file);
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			fout.write("HTTP/1.1 200 OK\r\n".getBytes());
			header.put("Server", "simpleWebServer 1.0\r\n");
			String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
			
			header.put("Content-Type", MimeTypeUtil.getMimeStrByExt(ext)+";charset=UTF-8\r\n");
			
			for (Entry<String, String> he : header.entrySet()) {
				fout.write(new String(he.getKey()+": "+he.getValue()).getBytes());
			}
			fout.write("\r\n".getBytes());
			fout.write(IOUtil.getByteByInputStream(fin));
			try {
				out.write(fout.toByteArray());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			ByteArrayOutputStream fout=new ByteArrayOutputStream();
			// can render 404
			//System.out.println("40444");
			try {
				fout.write("HTTP/1.1 404 OK\r\n".getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			header.put("Server", "simpleWebServer 1.0\r\n");
			header.put("Content-Type", "text/html;charset=UTF-8\r\n");
			try {
				fout.write("\r\n".getBytes());
				fout.write("<html><body>404 file not found</body></html>".getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				out.write(fout.toByteArray());
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void renderJson(Object json) {
		
	}
}
