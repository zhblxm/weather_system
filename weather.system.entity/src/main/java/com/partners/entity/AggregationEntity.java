package com.partners.entity;

import java.io.Serializable;

public class AggregationEntity implements Serializable {

	private double value;
	private String weatherStationName;
	private String weatherStationNumber;
	private String terminalName;
	private String terminalDesc;
	private String lastDate;
	private String type;
	private int terminalCategoryId;
	private int weatherStationId;
	public int getTerminalCategoryId() {
		return terminalCategoryId;
	}
	public void setTerminalCategoryId(int terminalCategoryId) {
		this.terminalCategoryId = terminalCategoryId;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getWeatherStationName() {
		return weatherStationName;
	}
	public void setWeatherStationName(String weatherStationName) {
		this.weatherStationName = weatherStationName;
	}
	public String getWeatherStationNumber() {
		return weatherStationNumber;
	}
	public void setWeatherStationNumber(String weatherStationNumber) {
		this.weatherStationNumber = weatherStationNumber;
	}
	public String getTerminalName() {
		return terminalName;
	}
	public void setTerminalName(String terminalName) {
		this.terminalName = terminalName;
	}

	public int getWeatherStationId() {
		return weatherStationId;
	}
	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}
	public String getTerminalDesc() {
		return terminalDesc;
	}
	public void setTerminalDesc(String terminalDesc) {
		this.terminalDesc = terminalDesc;
	}
	public String getLastDate() {
		return lastDate;
	}
	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
