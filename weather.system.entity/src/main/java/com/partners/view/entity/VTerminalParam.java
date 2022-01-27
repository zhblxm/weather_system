package com.partners.view.entity;

import java.sql.Timestamp;

public class VTerminalParam extends vBaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = -929594699878077184L;

	public VTerminalParam(){
	}
	public VTerminalParam(vBaseEntity entity) {
		super(entity);
	}
	public VTerminalParam(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size, String uniqueId) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	private String uniqueId;

}
