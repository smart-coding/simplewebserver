package com.fzb.http.mimetype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class MimeTypeUtil {

	private static Map<String,String> map=new HashMap<String,String>();
	static{
		Properties prop=new Properties();
		try {
			prop.load(new FileInputStream(new File(new File(MimeTypeUtil.class.getClass().getResource("/").getPath()).getParentFile()+"/conf/"+"mimetype.properties")));
			for(Entry<Object, Object> p:prop.entrySet()){
				map.put(p.getKey().toString(), p.getValue().toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String getMimeStrByExt(String ext){
		return map.get(ext);
	}
}
