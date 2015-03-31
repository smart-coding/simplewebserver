package com.fzb.http.kit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigKit {

	public static Integer getMaxUploadSize(){
		Properties prop=new Properties();
		try {
			prop.load(new FileInputStream(PathKit.getConfFile("/conf.properties")));
			return Integer.parseInt(prop.get("server.maxUploadSize").toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return 20971520;
	}
}
