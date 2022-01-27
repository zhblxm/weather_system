package com.partners.entity;

public class SystemOption implements java.io.Serializable {

	private static final long serialVersionUID = -2701235081208080022L;
	private String optionId;
	private String optionValue;
	private char isSystem;
	private int version;
	public String getOptionId() {
		return optionId;
	}
	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}
	public String getOptionValue() {
		return optionValue;
	}
	public void setOptionValue(String optionValue) {
		this.optionValue = optionValue;
	}
	public char getIsSystem() {
		return isSystem;
	}
	public void setIsSystem(char isSystem) {
		this.isSystem = isSystem;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
