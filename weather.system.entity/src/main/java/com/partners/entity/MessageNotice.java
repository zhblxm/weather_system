package com.partners.entity;

public class MessageNotice extends BaseEntity implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7078902094456704573L;
	private Integer messageNoticeId;
	private String uniqueMessageNoticeId;
	public String getUniqueMessageNoticeId() {
		return uniqueMessageNoticeId;
	}
	public void setUniqueMessageNoticeId(String uniqueMessageNoticeId) {
		this.uniqueMessageNoticeId = uniqueMessageNoticeId;
	}
	private int messageNoticeType;
	private String messageNoticeDesc;
	private String message;
	private byte isChecked;
	private Character extendInfo;
	
	public Integer getMessageNoticeId() {
		return messageNoticeId;
	}
	public void setMessageNoticeId(Integer messageNoticeId) {
		this.messageNoticeId = messageNoticeId;
	}
	public int getMessageNoticeType() {
		return messageNoticeType;
	}
	public void setMessageNoticeType(int messageNoticeType) {
		this.messageNoticeType = messageNoticeType;
	}
	public String getMessageNoticeDesc() {
		return messageNoticeDesc;
	}
	public void setMessageNoticeDesc(String messageNoticeDesc) {
		this.messageNoticeDesc = messageNoticeDesc;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public byte getIsChecked() {
		return isChecked;
	}
	public void setIsChecked(byte isChecked) {
		this.isChecked = isChecked;
	}
	public Character getExtendInfo() {
		return extendInfo;
	}
	public void setExtendInfo(Character extendInfo) {
		this.extendInfo = extendInfo;
	}
}
