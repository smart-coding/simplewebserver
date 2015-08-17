package com.fzb.demo.file;

import java.io.Serializable;

public class SimpleFile implements Serializable {

    /**
     * 在线文件管理
     */
    private static final long serialVersionUID = -6435750015864524420L;
    private String fileName;
    private String fileSize;
    private boolean isFiler;
    private String path;
    private String fileDate;
    private String fullName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isFiler() {
        return isFiler;
    }

    public void setFiler(boolean isFiler) {
        this.isFiler = isFiler;
    }

    public String getView() {
        if (isFiler) {
            return "<div><input type='checkbox' name='path' value='" + fullName + "'><a href='/_file/fetch?folder=" + fullName + "'><span style='font-size:22px;'>" + fileName + "</span></a> <span style='color:gray'>文件夹</span></div>";
        } else {
            return "<div><input type='checkbox' name='path' value='" + fullName + "'><a href='" + fullName + "'><span style='color:gray'>" + fileName + "," + fileSize + "</span></a></div>";
        }
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
