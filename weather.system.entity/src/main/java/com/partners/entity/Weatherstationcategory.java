package com.partners.entity;
// Generated May 30, 2017 9:47:29 PM by Hibernate Tools 3.5.0.Final

public class Weatherstationcategory extends BaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = -6714117056943964191L;
	private int weatherStationCategoryId;
	private int parentCategoryId;
	private String uniqueCategoryId;
	private String uniqueParentId;
	private String weatherStationCategoryName;	
	private double longitude;
	private double latitude;
	private String parentCategoryName;

	public String getUniqueCategoryId() {
		return uniqueCategoryId;
	}

	public void setUniqueCategoryId(String uniqueCategoryId) {
		this.uniqueCategoryId = uniqueCategoryId;
	}

	public String getUniqueParentId() {
		return uniqueParentId;
	}

	public void setUniqueParentId(String uniqueParentId) {
		this.uniqueParentId = uniqueParentId;
	}

	public int getWeatherStationCategoryId() {
		return this.weatherStationCategoryId;
	}

	public void setWeatherStationCategoryId(int weatherStationCategoryId) {
		this.weatherStationCategoryId = weatherStationCategoryId;
	}

	public int getParentCategoryId() {
		return this.parentCategoryId;
	}

	public void setParentCategoryId(int parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}

	public String getWeatherStationCategoryName() {
		return weatherStationCategoryName;
	}

	public void setWeatherStationCategoryName(String weatherStationCategoryName) {
		this.weatherStationCategoryName = weatherStationCategoryName;
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

	public String getParentCategoryName() {
		return parentCategoryName;
	}

	public void setParentCategoryName(String parentCategoryName) {
		this.parentCategoryName = parentCategoryName;
	}
	
}
