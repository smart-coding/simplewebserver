package com.fzb.demo.file;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FilesManageUtil {


    public static List<SimpleFile> getSimpleFileByPath(String path, int root) {
        if (new File(path).isDirectory()) {
            File file[] = new File(path).listFiles();
            List<SimpleFile> list = new ArrayList<SimpleFile>();
            for (File f : file) {
                SimpleFile fs = new SimpleFile();
                fs.setFileDate(getStrBylong(f.lastModified()));
                fs.setFileName(f.getName());
                fs.setFiler(f.isDirectory());
                fs.setFileSize(formatSimpleFileSize(f.length()));
                fs.setPath(f.getParent().substring(root).replace("\\", "/") + "/");
                fs.setFullName(f.getParent().substring(root).replace("\\", "/") + "/" + f.getName());
                list.add(fs);
            }
            mySort(list);
            return list;
        } else {
            return null;
        }
    }

    public static List<SimpleFile> getSimpleFileByPath(String path, String root) {
        return getSimpleFileByPath(path, root.replace("\\", "/").length() - 1);
    }

    private long filerSize = 0;

    private long getFilerSize(String path) {
        File fp = new File(path);
        if (fp.isDirectory()) {
            File file[] = new File(path).listFiles();
            for (File f : file) {
                if (f.isDirectory()) {
                    getFilerSize(f.toString());
                } else {
                    filerSize += f.length();
                }
            }
        } else {
            filerSize += fp.length();
        }
        return filerSize;
    }

    public static String getDirSizeByPath(String path) {
        long siez = new FilesManageUtil().getFilerSize(path);
        return formatSimpleFileSize(siez);
    }

    public static void main(String[] args) {
        System.out.println(getDirSizeByPath("e:/home/ftp/t/test"));
        //System.out.println(getSimpleFileByPath("E:\\工具书\\"));
        //System.out.println(getContent("F:\\乱文件\\myblog (1).sql"));
        //System.out.println(delFile("E:\\resin-pro-3.1.12"));
        //moveOrCopy("F:\\test\\", "d:/pack/", false);

		/*Map<String,Object> map=new HashMap<String,Object>();
		map.put("tag", "e:\\ee");
		map.put("source", "e:\\eclipse-standard-kepler-SR2-win32.zip");
		
		unZip(map);*/
        delFiler("E:/workspace/ROOT");

    }

    public static Object[] getContent(String file) {
        Object[] obj = new Object[10];
        try {
            File f = new File(file);
            if (f.length() > 1024 * 1024) {
                obj[0] = "";
                obj[1] = "文件过大(超过1M) 无法编辑";
                return obj;
            }
            InputStream in = new FileInputStream(f);
            byte b[] = new byte[1];
            StringBuffer sb = new StringBuffer();
            while (in.read(b) != -1) {
                sb.append(new String(b));
                b = new byte[1];
            }
            obj[0] = sb.toString().trim();
            obj[1] = "utf-8";
            return obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean delFile(String file) {

        File f = new File(file);
        if (f.isDirectory()) {

            delFiler(file);
            return true;
        } else {
            return new File(file).delete();
        }
    }

    public static void delFiler(String filer) {
        File f = new File(filer);
        if (f.isDirectory()) {
            File fs[] = new File(filer).listFiles();
            for (File fl : fs) {
                if (fl.isDirectory() && fl.listFiles().length != 0) {
                    delFile(fl.toString());
                } else {
                    fl.delete();
                }
            }
        }
        f.delete();
    }

    public static void moveOrCopy(String filer, String tag, boolean isMove) {
        File f = new File(filer);
        if (f.isDirectory()) {
            File fs[] = new File(filer).listFiles();
            tag = tag + "/" + f.getName();
            new File(tag).mkdir();
            for (File fl : fs) {
                if (fl.isDirectory()) {
                    moveOrCopy(fl.toString(), tag, isMove);

                } else {
                    moveOrCopyFile(fl.toString(), tag + "/" + fl.getName(), isMove);
                }
            }
        } else {
            moveOrCopyFile(f.toString(), tag + "/" + f.getName(), isMove);
        }
    }

    public static void moveOrCopyFile(String src, String tag, boolean isMove) {
        try {

            File f = new File(src);
            FileInputStream in = new FileInputStream(src);
            int SimpleFileize = (int) f.length();
            byte b[] = null;
            FileOutputStream out = new FileOutputStream(tag);
            int cnt = SimpleFileize / (1024 * 1024);
            System.out.println(src);
            if (cnt == 0) {
                b = new byte[SimpleFileize];
            } else {
                b = new byte[1024 * 1024];
                while (in.read(b) != -1) {
                    out.write(b);
                    if (--cnt == 0) {
                        break;
                    }
                    b = new byte[1024 * 1024];
                }
                b = new byte[SimpleFileize % (1024 * 1024)];
            }
            in.read(b);
            in.close();
            out.write(b);
            if (isMove) {
                f.delete();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println(src);
        //System.out.println(tag);
    }

    public static void copyFileByInStream(InputStream in, String tag) {
        try {
            OutputStream out = new FileOutputStream(new File(tag));
            byte b[] = new byte[1];
            while (in.read(b) != -1) {
                out.write(b);
                b = new byte[1];
            }
            in.read(b);
            in.close();
            out.write(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatSimpleFileSize(long simpleFile) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String simpleFileSizeString;
        if (simpleFile < 1024) {
            simpleFileSizeString = df.format((double) simpleFile) + " B";
        } else if (simpleFile < 1048576) {
            simpleFileSizeString = df.format((double) simpleFile / 1024) + " K";
        } else if (simpleFile < 1073741824) {
            simpleFileSizeString = df.format((double) simpleFile / 1048576) + " M";
        } else {
            simpleFileSizeString = df.format((double) simpleFile / 1073741824) + " G";
        }
        return simpleFileSizeString;
    }

    public static void mySort(List<SimpleFile> SimpleFile) {
        Collections.sort(SimpleFile, new Comparator<SimpleFile>() {
            public int compare(SimpleFile o1, SimpleFile o2) {

                //如果传进来的2个文件o1为文件夹 o2 不是文件时需要改变顺序
                if (o1.isFiler() && !o2.isFiler())
                    return -1;
                //返回值>=0，则不调用Arrays.swap(Object x[], int a, int b) 方法。
                if (!o1.isFiler() && o2.isFiler())
                    return 1;
                return 0;
            }
        });
    }

    public static void createFiler(String root) {
        new File(root).mkdirs();
    }

    public static boolean modifyName(String src, String now) {
        if (checkFileIsExists(src)) {
            new File(src).renameTo(new File(now));
            return true;
        }
        return false;
    }

    public static void modifyFile(String root, String code, String content) {

    }

	/*public static boolean unZip(Map<String, Object> map){
        try {
        	CompressUtil.unzip(map.get("source").toString(), map.get("tag").toString(), null);
		} catch (ZipException e) {
			e.printStackTrace();
		}
        return true;
	}
	
	public static boolean inZip(Map<String, Object> map){
		CompressUtil.zip(map.get("source").toString(),map.get("tag").toString(), null);
		return true;
	}*/

    public static boolean checkFileIsExists(String string) {
        return new File(string).canExecute();
    }

    public static String getStrBylong(long lon) {
        return new SimpleDateFormat().format(new Date(lon * 1000));
    }

}
