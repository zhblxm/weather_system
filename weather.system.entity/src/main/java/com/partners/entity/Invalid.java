package com.partners.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Invalid {

	private String invalidField;
	private double minValue;
	private double maxValue;
	private double value;
	private int stationId;
	private String weatherStationName;
	private String weatherStationNumber;
	private String invalidFieldDesc;
	private String covert;
	private Timestamp collectionDate;

	public String getCovert() {
		return covert;
	}

	public void setCovert(String covert) {
		this.covert = covert;
	}

	public String getInvalidField() {
		return invalidField;
	}

	public void setInvalidField(String invalidField) {
		this.invalidField = invalidField;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
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

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getInvalidFieldDesc() {
		return invalidFieldDesc;
	}

	public void setInvalidFieldDesc(String invalidFieldDesc) {
		this.invalidFieldDesc = invalidFieldDesc;
	}
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	public Timestamp getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(Timestamp collectionDate) {
		this.collectionDate = collectionDate;
	}

}
