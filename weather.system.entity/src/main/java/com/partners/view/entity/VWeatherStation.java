package com.partners.view.entity;

import java.sql.Timestamp;
import java.util.List;

public class VWeatherStation extends vBaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 1605090667573794615L;
	private String uniqueCategoryId;
	private List<Integer> stations;
	public VWeatherStation(vBaseEntity entity) {
		super(entity);
	}
	public VWeatherStation(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size, String uniqueCategoryId) {
		super(createDate, name, id, orderField, orderType, startIndex, size);
		this.uniqueCategoryId = uniqueCategoryId;
	}

	public String getUniqueCategoryId() {
		return uniqueCategoryId;
	}

	public void setUniqueCategoryId(String uniqueCategoryId) {
		this.uniqueCategoryId = uniqueCategoryId;
	}
	public List<Integer> getStations() {
		return stations;
	}
	public void setStations(List<Integer> stations) {
		this.stations = stations;
	}
	
}
