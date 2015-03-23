package com.fzb.http.server;

import java.io.File;
import java.io.OutputStream;

public interface HttpResponse {

	void wirteFile(File file);
	void renderHtml(String urlPath);
	void renderJson(Object json);
	OutputStream getWirter();
	void setHeader(String key,String value);
	void renderError(int errorCode);
}
