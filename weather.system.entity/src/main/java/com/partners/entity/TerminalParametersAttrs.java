package com.partners.entity;

import java.util.List;

public class TerminalParametersAttrs implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private String showInPage;
	private int order;
	private String orderDesc;
	private String id;
	private boolean customeFiled;
	private List<ParameterAttribute> parameterAttributes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getOrderDesc() {
		return orderDesc;
	}

	public void setOrderDesc(String orderDesc) {
		this.orderDesc = orderDesc;
	}

	public TerminalParametersAttrs(String name) {
		this.name = name;
	}
	public TerminalParametersAttrs(String name,boolean customeField) {
		this.name = name;
		this.customeFiled=customeField;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShowInPage() {
		return showInPage;
	}

	public void setShowInPage(String showInPage) {
		this.showInPage = showInPage;
	}

	public List<ParameterAttribute> getParameterAttributes() {
		return parameterAttributes;
	}

	public void setParameterAttributes(List<ParameterAttribute> parameterAttributes) {
		this.parameterAttributes = parameterAttributes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isCustomeFiled() {
		return customeFiled;
	}

	public void setCustomeFiled(boolean customeFiled) {
		this.customeFiled = customeFiled;
	}

}
