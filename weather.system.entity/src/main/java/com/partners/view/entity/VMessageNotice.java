package com.partners.view.entity;

import java.sql.Timestamp;

public class VMessageNotice extends vBaseEntity {
	public VMessageNotice() {
	}
	public VMessageNotice(vBaseEntity entity) {
		super(entity);
	}
	public VMessageNotice(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
	}
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
	private String uniqueId;
	private byte isForceChangePwd;
}
