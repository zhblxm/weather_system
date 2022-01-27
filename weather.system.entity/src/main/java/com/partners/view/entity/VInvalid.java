package com.partners.view.entity;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class VInvalid extends vBaseEntity {
	private String startDate;
	private String endDate;
	private List<Integer> weatherStations;
	private List<String> parameters;
	private int totalRecords;
	public VInvalid(vBaseEntity entity) {
		super(entity);
	}

	public VInvalid(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
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

	public List<Integer> getWeatherStations() {
		return weatherStations;
	}

	public void setWeatherStations(List<Integer> weatherStations) {
		this.weatherStations = weatherStations;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

}
