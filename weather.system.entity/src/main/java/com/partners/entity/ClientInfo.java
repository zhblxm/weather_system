package com.partners.entity;
// Generated May 30, 2017 9:47:29 PM by Hibernate Tools 3.5.0.Final

public class ClientInfo implements java.io.Serializable {

	public ClientInfo() {
		super();
	}
	public ClientInfo(String clientIP, int port) {
		super();
		this.clientIP = clientIP;
		this.port = port;
	}

	private static final long serialVersionUID = 3310356026841690213L;
	private int clientId;
	private String clientIP;
	private int port;
	private String weatherStationNumber;

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getWeatherStationNumber() {
		return weatherStationNumber;
	}

	public void setWeatherStationNumber(String weatherStationNumber) {
		this.weatherStationNumber = weatherStationNumber;
	}

}
