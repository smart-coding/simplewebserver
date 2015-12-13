package com.fzb.http.kit;

import java.io.File;

/**
 * 提供给一些路径供程序更方便的调用
 *
 * @author Chun
 */
public class PathKit {

    public static String getConfPath() {
        return getRootPath() + "/conf/";
    }

    public static String getRootPath() {
        String path;
        if (PathKit.class.getResource("/") != null) {
            path = new File(PathKit.class.getClass().getResource("/").getPath()).getParentFile().getParentFile().toString();

        } else {
            String thisPath = PathKit.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("\\", "/");
            path = thisPath.substring(0, thisPath.lastIndexOf('/'));
        }
        return path;
    }

    public static String getConfFile(String file) {
        return getConfPath() + file;
    }

    public static String getStaticPath() {
        return getRootPath() + "/static/";
    }

    public static String getTempPath() {
        String str = getRootPath() + "/temp/";
        new File(str).mkdirs();
        return str;
    }
}
