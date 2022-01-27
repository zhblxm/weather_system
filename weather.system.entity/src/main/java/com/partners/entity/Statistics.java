package com.partners.entity;

public class Statistics implements java.io.Serializable {
	private static final long serialVersionUID = -1012507369302026446L;
	private String weatherStationName;
	private String weatherStationNumber;
	private String weatherStationCategoryName;
	private long onTimeCount;
	private String onTimePercent;
	private long delayedTimeCount;
	private String delayedTimePercent;	
	private long loseCount;
	private String losePercent;	
	private long totalCount;
	
	public Statistics(String weatherStationName, String weatherStationNumber, String weatherStationCategoryName) {
		this.weatherStationName = weatherStationName;
		this.weatherStationNumber = weatherStationNumber;
		this.weatherStationCategoryName = weatherStationCategoryName;
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

	public String getOnTimePercent() {
		return onTimePercent;
	}

	public void setOnTimePercent(String onTimePercent) {
		this.onTimePercent = onTimePercent;
	}

	public long getDelayedTimeCount() {
		return delayedTimeCount;
	}

	public void setDelayedTimeCount(long delayedTimeCount) {
		this.delayedTimeCount = delayedTimeCount;
	}

	public String getDelayedTimePercent() {
		return delayedTimePercent;
	}

	public void setDelayedTimePercent(String delayedTimePercent) {
		this.delayedTimePercent = delayedTimePercent;
	}

	public long getLoseCount() {
		return loseCount;
	}

	public void setLoseCount(long loseCount) {
		this.loseCount = loseCount;
	}

	public String getLosePercent() {
		return losePercent;
	}

	public void setLosePercent(String losePercent) {
		this.losePercent = losePercent;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

}
