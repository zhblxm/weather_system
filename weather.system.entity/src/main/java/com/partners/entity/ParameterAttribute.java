package com.partners.entity;

public class ParameterAttribute implements java.io.Serializable {

	private static final long serialVersionUID = 5185489574853753620L;
	private String description;
	private String name;
	private String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
