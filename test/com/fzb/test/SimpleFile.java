package com.fzb.test;

import java.io.File;
import java.io.Serializable;

public class SimpleFile implements Serializable{

	/**
	 * 在线文件管理
	 */
	private static final long serialVersionUID = -6435750015864524420L;
	private String fileName;
	private String fileSzie;
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
	public String getFileSzie() {
		return fileSzie;
	}
	public void setFileSzie(String fileSzie) {
		this.fileSzie = fileSzie;
	}
	public boolean isFiler() {
		return isFiler;
	}
	public void setFiler(boolean isFiler) {
		this.isFiler = isFiler;
	}
	public String getView() {
		if(isFiler){
			return "<div class='content'><input type='checkbox' name='path' value='"+fullName+"'><a href='/_file/fetch?folder="+fullName+"'><span style='font-size:22px;'>"+fileName+"</span></a> <span style='color:gray'>文件夹</span></div>";
		}
		else{
			return "<div class='content2'><input type='checkbox' name='path' value='"+fullName+"'><a href='"+fullName+"'><span style='color:gray'>"+fileName+","+fileSzie+"</span></a></div>";
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
