package com.partners.entity;

import java.io.Serializable;

public class RequestMessage implements Serializable {

	private static final long serialVersionUID = 7960171418870139074L;
	private String ClientIP;
	private int Port;
	private String RequestMessage;

	public String getClientIP() {
		return ClientIP;
	}

	public void setClientIP(String clientIP) {
		ClientIP = clientIP;
	}

	public int getPort() {
		return Port;
	}

	public void setPort(int port) {
		Port = port;
	}

	public String getRequestMessage() {
		return RequestMessage;
	}

	public void setRequestMessage(String requestMessage) {
		RequestMessage = requestMessage;
	}

}
