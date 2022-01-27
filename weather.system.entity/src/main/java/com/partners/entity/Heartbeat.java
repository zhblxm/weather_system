package com.partners.entity;

import org.joda.time.DateTime;

public class Heartbeat implements java.io.Serializable  {

	private static final long serialVersionUID = 1L;

	private String weatherStationNumber;
	private DateTime createDate = DateTime.now();
	public String getWeatherStationNumber() {
		return weatherStationNumber;
	}
	public void setWeatherStationNumber(String weatherStationNumber) {
		this.weatherStationNumber = weatherStationNumber;
	}
	public DateTime getCreateDate() {
		return createDate;
	}
	public void setCreateDate(DateTime createDate) {
		this.createDate = createDate;
	}

}
