package com.fzb.http.mimetype;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.fzb.http.kit.PathKit;

public class MimeTypeUtil {

	private static Map<String,String> map=new HashMap<String,String>();
	static{
		Properties prop=new Properties();
		try {
			prop.load(new FileInputStream(PathKit.getConfFile("mimetype.properties")));
			for(Entry<Object, Object> p:prop.entrySet()){
				map.put(p.getKey().toString(), p.getValue().toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getMimeStrByExt(String ext){
		return map.get(ext);
	}
}
