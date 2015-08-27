package com.fzb.http.kit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigKit {

    private static Properties prop;

    static {
        prop = new Properties();
        try {
            prop.load(new FileInputStream(PathKit.getConfFile("/conf.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Integer getMaxUploadSize() {
        Object maxUploadSize = prop.get("server.maxUploadSize");
        if (maxUploadSize != null) {
            return Integer.parseInt(maxUploadSize.toString());
        }
        return 20971520;
    }

    public static Integer getServerPort() {
        Object port = prop.get("server.port");
        if (port != null) {
            return Integer.parseInt(port.toString());
        }
        return 6058;
    }
}
