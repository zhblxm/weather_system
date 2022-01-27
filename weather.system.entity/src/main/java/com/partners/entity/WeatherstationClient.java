package com.partners.entity;

public class WeatherstationClient implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8208283452560305771L;
	private int weatherStationTerminalId;
	private String terminalModel;
	private int teminalParameterCategoryId;
	private int weatherStationId;
	private String acquisitionFrequencyUnit;
	private int acquisitionFrequency;
	private String weatherStationNumber;
	private int clientId;
	private String clientIP;
	private int port;
	public int getWeatherStationTerminalId() {
		return weatherStationTerminalId;
	}
	public void setWeatherStationTerminalId(int weatherStationTerminalId) {
		this.weatherStationTerminalId = weatherStationTerminalId;
	}
	public String getTerminalModel() {
		return terminalModel;
	}
	public void setTerminalModel(String terminalModel) {
		this.terminalModel = terminalModel;
	}
	public int getTeminalParameterCategoryId() {
		return teminalParameterCategoryId;
	}
	public void setTeminalParameterCategoryId(int teminalParameterCategoryId) {
		this.teminalParameterCategoryId = teminalParameterCategoryId;
	}
	public int getWeatherStationId() {
		return weatherStationId;
	}
	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}
	public String getAcquisitionFrequencyUnit() {
		return acquisitionFrequencyUnit;
	}
	public void setAcquisitionFrequencyUnit(String acquisitionFrequencyUnit) {
		this.acquisitionFrequencyUnit = acquisitionFrequencyUnit;
	}
	public int getAcquisitionFrequency() {
		return acquisitionFrequency;
	}
	public void setAcquisitionFrequency(int acquisitionFrequency) {
		this.acquisitionFrequency = acquisitionFrequency;
	}
	public String getWeatherStationNumber() {
		return weatherStationNumber;
	}
	public void setWeatherStationNumber(String weatherStationNumber) {
		this.weatherStationNumber = weatherStationNumber;
	}
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
