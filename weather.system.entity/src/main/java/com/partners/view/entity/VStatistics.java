package com.partners.view.entity;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class VStatistics extends vBaseEntity {
	private String startDate;
	private String endDate;
	private String weatherStationName;
	private int weatherStationId;
	private List<Integer> weatherStations;

	public VStatistics(vBaseEntity entity) {
		super(entity);
	}

	public VStatistics(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getWeatherStationName() {
		return weatherStationName;
	}

	public void setWeatherStationName(String weatherStationName) {
		this.weatherStationName = weatherStationName;
	}

	public int getWeatherStationId() {
		return weatherStationId;
	}

	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}

	public List<Integer> getWeatherStations() {
		return weatherStations;
	}

	public void setWeatherStations(List<Integer> weatherStations) {
		this.weatherStations = weatherStations;
	}

}
