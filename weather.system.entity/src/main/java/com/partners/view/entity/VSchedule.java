package com.partners.view.entity;

import java.sql.Timestamp;

public class VSchedule extends vBaseEntity {
	public VSchedule() {
	}
	public VSchedule(vBaseEntity entity) {
		super(entity);
	}
	public VSchedule(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
	}
	private String uniqueId;
	private byte isForceChangePwd;
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public byte getIsForceChangePwd() {
		return isForceChangePwd;
	}
	public void setIsForceChangePwd(byte isForceChangePwd) {
		this.isForceChangePwd = isForceChangePwd;
	}
	
}
