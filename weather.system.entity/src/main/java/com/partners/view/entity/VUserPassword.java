package com.partners.view.entity;

public class VUserPassword {

	private String originPassowrd;
	private String newPassword;
	private byte isForcePwd;
	public String getOriginPassowrd() {
		return originPassowrd;
	}

	public void setOriginPassowrd(String originPassowrd) {
		this.originPassowrd = originPassowrd;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public byte getIsForcePwd() {
		return isForcePwd;
	}

	public void setIsForcePwd(byte isForcePwd) {
		this.isForcePwd = isForcePwd;
	}
	
}
