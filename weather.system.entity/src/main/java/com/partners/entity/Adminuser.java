package com.partners.entity;
// Generated May 30, 2017 9:47:29 PM by Hibernate Tools 3.5.0.Final

import java.util.List;

public class Adminuser extends BaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer userId;
	private Integer groupId;
	private String userName;
	private String userPassword;
	private byte isForceChangePwd;
	private List<Integer> permissions;
	private String userEmail;
	private String uniqueUserId;
	private String uniqueGroupId;
	private String weatherStation;
	private String weatherStationGroup;
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public String getUniqueUserId() {
		return uniqueUserId;
	}

	public void setUniqueUserId(String uniqueUserId) {
		this.uniqueUserId = uniqueUserId;
	}

	public String getUniqueGroupId() {
		return uniqueGroupId;
	}

	public void setUniqueGroupId(String uniqueGroupId) {
		this.uniqueGroupId = uniqueGroupId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public List<Integer> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Integer> permissions) {
		this.permissions = permissions;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return this.userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public byte getIsForceChangePwd() {
		return this.isForceChangePwd;
	}

	public void setIsForceChangePwd(byte isForceChangePwd) {
		this.isForceChangePwd = isForceChangePwd;
	}
	public String getWeatherStation() {
		return weatherStation;
	}
	public void setWeatherStation(String weatherStation) {
		this.weatherStation = weatherStation;
	}
	public String getWeatherStationGroup() {
		return weatherStationGroup;
	}
	public void setWeatherStationGroup(String weatherStationGroup) {
		this.weatherStationGroup = weatherStationGroup;
	}
	
}
