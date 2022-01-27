package com.partners.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditTrailLog  implements java.io.Serializable {

	private static final long serialVersionUID = 5101352225616767094L;
	private long userAuditTrailLogId;
	private String userName;
	private String description;
	private String clientIP;
	private int userActionId;
	private String userActionName;
	private Timestamp requestDate;
	private String requestDetail;
	public long getUserAuditTrailLogId() {
		return userAuditTrailLogId;
	}
	public void setUserAuditTrailLogId(long userAuditTrailLogId) {
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
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
