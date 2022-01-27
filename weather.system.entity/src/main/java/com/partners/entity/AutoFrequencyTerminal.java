package com.partners.entity;

import java.io.Serializable;

public class AutoFrequencyTerminal implements Serializable {

	private static final long serialVersionUID = -5784816836339793023L;

	private int weatherStationId;
	private int weatherStationTerminalId;
	private int originalStationTerminalId;
	private String terminalModel;
	
	public AutoFrequencyTerminal(int weatherStationId, int weatherStationTerminalId,int originalStationTerminalId,String terminalModel) {
		this.weatherStationId=weatherStationId;
		this.weatherStationTerminalId=weatherStationTerminalId;
		this.originalStationTerminalId=originalStationTerminalId;
		this.terminalModel=terminalModel;
	}
	public int getWeatherStationId() {
		return weatherStationId;
	}

	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}

	public int getWeatherStationTerminalId() {
		return weatherStationTerminalId;
	}

	public void setWeatherStationTerminalId(int weatherStationTerminalId) {
		this.weatherStationTerminalId = weatherStationTerminalId;
	}

	public int getOriginalStationTerminalId() {
		return originalStationTerminalId;
	}

	public void setOriginalStationTerminalId(int originalStationTerminalId) {
		this.originalStationTerminalId = originalStationTerminalId;
	}

	public String getTerminalModel() {
		return terminalModel;
	}

	public void setTerminalModel(String terminalModel) {
		this.terminalModel = terminalModel;
	}

}
