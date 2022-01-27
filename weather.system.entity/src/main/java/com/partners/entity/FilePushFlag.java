package com.partners.entity;

public class FilePushFlag {
	private int id;
	private String flag;
	private String defaultValue;
	private String terminalparam;
	public String getTerminalparam() {
		return terminalparam;
	}
	public void setTerminalparam(String terminalparam) {
		this.terminalparam = terminalparam;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
