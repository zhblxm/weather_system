package com.partners.entity;

import java.util.ArrayList;

public class Weatherstation extends BaseEntity implements java.io.Serializable {
	private static final long serialVersionUID = -8768770958550896468L;
	private int weatherStationId;
	private int weatherStationCategoryId;
	private String weatherStationName;
	private double longitude;
	private double latitude;
	private String weatherStationNumber;
	private double altitude;
	private String contactUserName;
	private String contactPhone;
	private String emergencyPhone;
	private String gRPSPort;
	private int isAutoSynTerminal;
	private int autoSynFrequency;
	private String autoSynFrequencyUnit;
	private String extendInfo;
	private String uniqueWeatherStationId;
	private String uniqueCategoryId;
	private ArrayList<WeatherStationTerminal> weatherStationTerminals;
	private String weatherStationCategoryName;
	private long onTimeCount;
	private long receiveCount;
	private long delayedTimeCount;
	private long loseCount;
	private long totalCount;
	private String cover;
	private double voltage;
	private String voltagePercent;
	public int getWeatherStationId() {
		return weatherStationId;
	}

	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}

	public int getWeatherStationCategoryId() {
		return weatherStationCategoryId;
	}

	public void setWeatherStationCategoryId(int weatherStationCategoryId) {
		this.weatherStationCategoryId = weatherStationCategoryId;
	}

	public String getWeatherStationName() {
		return weatherStationName;
	}

	public void setWeatherStationName(String weatherStationName) {
		this.weatherStationName = weatherStationName;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getWeatherStationNumber() {
		return weatherStationNumber;
	}

	public void setWeatherStationNumber(String weatherStationNumber) {
		this.weatherStationNumber = weatherStationNumber;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public String getContactUserName() {
		return contactUserName;
	}

	public void setContactUserName(String contactUserName) {
		this.contactUserName = contactUserName;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getEmergencyPhone() {
		return emergencyPhone;
	}

	public void setEmergencyPhone(String emergencyPhone) {
		this.emergencyPhone = emergencyPhone;
	}

	public String getgRPSPort() {
		return gRPSPort;
	}

	public void setgRPSPort(String gRPSPort) {
		this.gRPSPort = gRPSPort;
	}

	public int getIsAutoSynTerminal() {
		return isAutoSynTerminal;
	}

	public void setIsAutoSynTerminal(int isAutoSynTerminal) {
		this.isAutoSynTerminal = isAutoSynTerminal;
	}

	public int getAutoSynFrequency() {
		return autoSynFrequency;
	}

	public void setAutoSynFrequency(int autoSynFrequency) {
		this.autoSynFrequency = autoSynFrequency;
	}

	public String getExtendInfo() {
		return extendInfo;
	}

	public void setExtendInfo(String extendInfo) {
		this.extendInfo = extendInfo;
	}

	public String getUniqueWeatherStationId() {
		return uniqueWeatherStationId;
	}

	public void setUniqueWeatherStationId(String uniqueWeatherStationId) {
		this.uniqueWeatherStationId = uniqueWeatherStationId;
	}

	public String getUniqueCategoryId() {
		return uniqueCategoryId;
	}

	public void setUniqueCategoryId(String uniqueCategoryId) {
		this.uniqueCategoryId = uniqueCategoryId;
	}

	public String getAutoSynFrequencyUnit() {
		return autoSynFrequencyUnit;
	}

	public void setAutoSynFrequencyUnit(String autoSynFrequencyUnit) {
		this.autoSynFrequencyUnit = autoSynFrequencyUnit;
	}

	public ArrayList<WeatherStationTerminal> getWeatherStationTerminals() {
		return weatherStationTerminals;
	}

	public void setWeatherStationTerminals(ArrayList<WeatherStationTerminal> weatherStationTerminals) {
		this.weatherStationTerminals = weatherStationTerminals;
	}

	public String getWeatherStationCategoryName() {
		return weatherStationCategoryName;
	}

	public void setWeatherStationCategoryName(String weatherStationCategoryName) {
		this.weatherStationCategoryName = weatherStationCategoryName;
	}

	public long getOnTimeCount() {
		return onTimeCount;
	}

	public void setOnTimeCount(long onTimeCount) {
		this.onTimeCount = onTimeCount;
	}

	public long getDelayedTimeCount() {
		return delayedTimeCount;
	}

	public void setDelayedTimeCount(long delayedTimeCount) {
		this.delayedTimeCount = delayedTimeCount;
	}

	public long getLoseCount() {
		return loseCount;
	}

	public void setLoseCount(long loseCount) {
		this.loseCount = loseCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getReceiveCount() {
		return receiveCount;
	}

	public void setReceiveCount(long receiveCount) {
		this.receiveCount = receiveCount;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public String getVoltagePercent() {
		return voltagePercent;
	}

	public void setVoltagePercent(String voltagePercent) {
		this.voltagePercent = voltagePercent;
	}
	
}
