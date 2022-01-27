package com.partners.view.entity;

import java.sql.Timestamp;

public class VAuditTrailLog extends vBaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 5101352225616767094L;
	private int userAuditTrailLogId;
	private String userName;
	private String description;
	private String clientIP;
	private int userActionId;
	private String userActionName;
	private Timestamp requestDate;
	private String requestDetail;
	public VAuditTrailLog(vBaseEntity entity)
	{
		super(entity);
	}
	public VAuditTrailLog(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
	}
	
	public int getUserAuditTrailLogId() {
		return userAuditTrailLogId;
	}
	public void setUserAuditTrailLogId(int userAuditTrailLogId) {
		this.userAuditTrailLogId = userAuditTrailLogId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
	public int getUserActionId() {
		return userActionId;
	}
	public void setUserActionId(int userActionId) {
		this.userActionId = userActionId;
	}
	public String getUserActionName() {
		return userActionName;
	}
	public void setUserActionName(String userActionName) {
		this.userActionName = userActionName;
	}
	public Timestamp getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(Timestamp requestDate) {
		this.requestDate = requestDate;
	}
	public String getRequestDetail() {
		return requestDetail;
	}
	public void setRequestDetail(String requestDetail) {
		this.requestDetail = requestDetail;
	}

}
