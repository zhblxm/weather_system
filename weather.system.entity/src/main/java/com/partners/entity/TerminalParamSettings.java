package com.partners.entity;

public class TerminalParamSettings extends BaseEntity implements java.io.Serializable {
	private static final long serialVersionUID = -7637082730819092027L;
	private int terminalParamId;
	private int terminalParamCategoryId;
	private String terminalParamName;
	private String extendInfo;
	private String uniqueId;
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getTerminalParamId() {
		return terminalParamId;
	}

	public void setTerminalParamId(int terminalParamId) {
		this.terminalParamId = terminalParamId;
	}

	public int getTerminalParamCategoryId() {
		return terminalParamCategoryId;
	}

	public void setTerminalParamCategoryId(int terminalParamCategoryId) {
		this.terminalParamCategoryId = terminalParamCategoryId;
	}

	public String getTerminalParamName() {
		return terminalParamName;
	}

	public void setTerminalParamName(String terminalParamName) {
		this.terminalParamName = terminalParamName;
	}

	public String getExtendInfo() {
		return extendInfo;
	}

	public void setExtendInfo(String extendInfo) {
		this.extendInfo = extendInfo;
	}
}
