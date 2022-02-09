package com.partners.entity;

import lombok.Builder;

public class Terminalparameterscategory extends BaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private int terminalParamCategoryId;
	private String terminalParamCategoryName;
	private String mappingName;
	private int isSystem;
	 

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public Integer getTerminalParamCategoryId() {
		return this.terminalParamCategoryId;
	}

	public void setTerminalParamCategoryId(Integer terminalParamCategoryId) {
		this.terminalParamCategoryId = terminalParamCategoryId;
	}

	public String getTerminalParamCategoryName() {
		return this.terminalParamCategoryName;
	}

	public void setTerminalParamCategoryName(String terminalParamCategoryName) {
		this.terminalParamCategoryName = terminalParamCategoryName;
	}

	public String getCategoryUniqueId() {
		return categoryUniqueId;
	}
	public void setCategoryUniqueId(String categoryUniqueId) {
		this.categoryUniqueId = categoryUniqueId;
	}
	private String categoryUniqueId;
	private String parentCategoryUniqueId;

	public String getParentCategoryUniqueId() {
		return parentCategoryUniqueId;
	}

	public void setParentCategoryUniqueId(String parentCategoryUniqueId) {
		this.parentCategoryUniqueId = parentCategoryUniqueId;
	}
}
