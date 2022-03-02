package com.partners.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
public class RequestMessage implements Serializable {

	private static final long serialVersionUID = 7960171418870139074L;
	private String ClientIP;
	private int Port;
	private String RequestMessage;

	public RequestMessage() {
	}
}
