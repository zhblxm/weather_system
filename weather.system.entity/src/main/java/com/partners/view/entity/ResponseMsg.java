package com.partners.view.entity;

import java.io.Serializable;

public class ResponseMsg implements Serializable {

	private static final long serialVersionUID = 1L;
	private int statusCode;
	private String message;
	private Object messageObject;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getMessageObject() {
		return messageObject;
	}

	public void setMessageObject(Object messageObject) {
		this.messageObject = messageObject;
	}
}
