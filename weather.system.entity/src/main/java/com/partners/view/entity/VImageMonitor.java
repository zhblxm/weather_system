package com.partners.view.entity;

import java.util.List;

public class VImageMonitor {
	private int WeatherStationId;
	private int WeatherStationCategoryId;
	private String weatherStationName;
	private String weatherStationNumber;
	private List<VImageInfo> Images;
	public int getWeatherStationId() {
		return WeatherStationId;
	}
	public void setWeatherStationId(int weatherStationId) {
		WeatherStationId = weatherStationId;
	}
	public int getWeatherStationCategoryId() {
		return WeatherStationCategoryId;
	}
	public void setWeatherStationCategoryId(int weatherStationCategoryId) {
		WeatherStationCategoryId = weatherStationCategoryId;
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
	public List<VImageInfo> getImages() {
		return Images;
	}
	public void setImages(List<VImageInfo> images) {
		Images = images;
	}
	
}
