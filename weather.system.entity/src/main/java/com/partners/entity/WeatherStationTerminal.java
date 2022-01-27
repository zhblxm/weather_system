package com.partners.entity;

public class WeatherStationTerminal extends BaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 7105360345908589378L;

	private int weatherStationTerminalId;
	private String terminalModel;
	private int teminalParameterCategoryId;
	private int weatherStationId;
	private String acquisitionFrequencyUnit;
	private String acquisitionFrequencyDesc;
	private int acquisitionFrequency;
	private String uniqueTPCId;
	private String uniqueWSTId;
	private String uniqueStationId;
	private String terminalParameters;
	private String batteryType;
	public int getWeatherStationTerminalId() {
		return weatherStationTerminalId;
	}
	public void setWeatherStationTerminalId(int weatherStationTerminalId) {
		this.weatherStationTerminalId = weatherStationTerminalId;
	}
	public String getTerminalModel() {
		return terminalModel;
	}
	public void setTerminalModel(String terminalModel) {
		this.terminalModel = terminalModel;
	}
	public int getTeminalParameterCategoryId() {
		return teminalParameterCategoryId;
	}
	public void setTeminalParameterCategoryId(int teminalParameterCategoryId) {
		this.teminalParameterCategoryId = teminalParameterCategoryId;
	}
	public int getWeatherStationId() {
		return weatherStationId;
	}
	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}
	public String getAcquisitionFrequencyUnit() {
		return acquisitionFrequencyUnit;
	}
	public void setAcquisitionFrequencyUnit(String acquisitionFrequencyUnit) {
		this.acquisitionFrequencyUnit = acquisitionFrequencyUnit;
	}
	public String getAcquisitionFrequencyDesc() {
		return acquisitionFrequencyDesc;
	}
	public void setAcquisitionFrequencyDesc(String acquisitionFrequencyDesc) {
		this.acquisitionFrequencyDesc = acquisitionFrequencyDesc;
	}
	public int getAcquisitionFrequency() {
		return acquisitionFrequency;
	}
	public void setAcquisitionFrequency(int acquisitionFrequency) {
		this.acquisitionFrequency = acquisitionFrequency;
	}
	public String getUniqueTPCId() {
		return uniqueTPCId;
	}
	public void setUniqueTPCId(String uniqueTPCId) {
		this.uniqueTPCId = uniqueTPCId;
	}
	public String getUniqueWSTId() {
		return uniqueWSTId;
	}
	public void setUniqueWSTId(String uniqueWSTId) {
		this.uniqueWSTId = uniqueWSTId;
	}
	public String getUniqueStationId() {
		return uniqueStationId;
	}
	public void setUniqueStationId(String uniqueStationId) {
		this.uniqueStationId = uniqueStationId;
	}
	public String getTerminalParameters() {
		return terminalParameters;
	}
	public void setTerminalParameters(String terminalParameters) {
		this.terminalParameters = terminalParameters;
	}
	public String getBatteryType() {
		return batteryType;
	}
	public void setBatteryType(String batteryType) {
		this.batteryType = batteryType;
	}
	
}
