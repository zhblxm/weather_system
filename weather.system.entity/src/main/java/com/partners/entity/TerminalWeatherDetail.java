package com.partners.entity;

import java.io.Serializable;
import org.joda.time.DateTime;

public class TerminalWeatherDetail implements Serializable {

	private static final long serialVersionUID = 9152854130818522752L;
	/**
	 * 温度
	 */
	private double temperature;
	/**
	 * 湿度
	 */
	private double humidity;
	/**
	 * 合有效辐射/CO2浓度
	 */
	private double isValidRadiation;
	/**
	 *  电压
	 */
	private double voltage;
	/**
	 * ID号
	 */
	private String terminalNumber;
	/**
	 * 收集短格式日期
	 */
	private String ShortDate;
	/**
	 * 收集短格式时间
	 */
	private String ShortTime;
	/**
	 * 收集日期时间
	 */
	private String collectDate;
	/**
	 * CO2浓度
	 */
	private double concentration;
	/**
	 *  总辐射
	 */
	private double totlRadiation;
	/**
	 *  土壤温度
	 */
	private double soilTemperature10;
	private double soilTemperature20;
	private double soilTemperature30;
	private double soilTemperature40;
	private double soilTemperature50;
	private double soilTemperature60;
	/**
	 * 土壤湿度
	 */
	private double soilMoisture10;
	private double soilMoisture20;
	private double soilMoisture30;
	private double soilMoisture40;
	private double soilMoisture50;
	private double soilMoisture60;
	/**
	 * 风向
	 */
	private double windDirection;
	/**
	 * 风速
	 */
	private double windSpeed;
	/**
	 * 气压
	 */
	private double pressure;
	/**
	 * 雨量
	 */
	private double rainfall;
	/**
	 * 蒸发量
	 */
	private double evaporation;
	/**
	 * 日照时数
	 */
	private double sunshineNumber;
	/**
	 * 净辐射
	 */
	private double radiation;
	/**
	 * 设备header信息
	 */
	private String header;
	/**
	 * 时间
	 */
	private DateTime fullDate;
	/**
	 * 站点
	 */
	private int weatherStationId;
	/**
	 * 设备类型，便携，农业MBBA2和MBBA1,智能温室仪
	 */
	private TerminalTypeEnum terminalType;

	public int getWeatherStationId() {
		return weatherStationId;
	}

	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}

	public TerminalTypeEnum getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(TerminalTypeEnum terminalType) {
		this.terminalType = terminalType;
	}

	public DateTime getFullDate() {
		return fullDate;
	}

	public void setFullDate(DateTime fullDate) {
		this.fullDate = fullDate;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public double getIsValidRadiation() {
		return isValidRadiation;
	}

	public void setIsValidRadiation(double isValidRadiation) {
		this.isValidRadiation = isValidRadiation;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public String getTerminalNumber() {
		return terminalNumber;
	}

	public void setTerminalNumber(String terminalNumber) {
		this.terminalNumber = terminalNumber;
	}

	public String getShortDate() {
		return ShortDate;
	}

	public void setShortDate(String shortDate) {
		ShortDate = shortDate;
	}

	public String getShortTime() {
		return ShortTime;
	}

	public void setShortTime(String shortTime) {
		ShortTime = shortTime;
	}

	public String getCollectDate() {
		return collectDate;
	}

	public void setCollectDate(String collectDate) {
		this.collectDate = collectDate;
	}

	public double getConcentration() {
		return concentration;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}

	public double getTotlRadiation() {
		return totlRadiation;
	}

	public void setTotlRadiation(double totlRadiation) {
		this.totlRadiation = totlRadiation;
	}

	public double getSoilTemperature10() {
		return soilTemperature10;
	}

	public void setSoilTemperature10(double soilTemperature10) {
		this.soilTemperature10 = soilTemperature10;
	}

	public double getSoilTemperature20() {
		return soilTemperature20;
	}

	public void setSoilTemperature20(double soilTemperature20) {
		this.soilTemperature20 = soilTemperature20;
	}

	public double getSoilTemperature30() {
		return soilTemperature30;
	}

	public void setSoilTemperature30(double soilTemperature30) {
		this.soilTemperature30 = soilTemperature30;
	}

	public double getSoilTemperature40() {
		return soilTemperature40;
	}

	public void setSoilTemperature40(double soilTemperature40) {
		this.soilTemperature40 = soilTemperature40;
	}

	public double getSoilTemperature50() {
		return soilTemperature50;
	}

	public void setSoilTemperature50(double soilTemperature50) {
		this.soilTemperature50 = soilTemperature50;
	}

	public double getSoilTemperature60() {
		return soilTemperature60;
	}

	public void setSoilTemperature60(double soilTemperature60) {
		this.soilTemperature60 = soilTemperature60;
	}

	public double getSoilMoisture10() {
		return soilMoisture10;
	}

	public void setSoilMoisture10(double soilMoisture10) {
		this.soilMoisture10 = soilMoisture10;
	}

	public double getSoilMoisture20() {
		return soilMoisture20;
	}

	public void setSoilMoisture20(double soilMoisture20) {
		this.soilMoisture20 = soilMoisture20;
	}

	public double getSoilMoisture30() {
		return soilMoisture30;
	}

	public void setSoilMoisture30(double soilMoisture30) {
		this.soilMoisture30 = soilMoisture30;
	}

	public double getSoilMoisture40() {
		return soilMoisture40;
	}

	public void setSoilMoisture40(double soilMoisture40) {
		this.soilMoisture40 = soilMoisture40;
	}

	public double getSoilMoisture50() {
		return soilMoisture50;
	}

	public void setSoilMoisture50(double soilMoisture50) {
		this.soilMoisture50 = soilMoisture50;
	}

	public double getSoilMoisture60() {
		return soilMoisture60;
	}

	public void setSoilMoisture60(double soilMoisture60) {
		this.soilMoisture60 = soilMoisture60;
	}

	public double getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(double windDirection) {
		this.windDirection = windDirection;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public double getRainfall() {
		return rainfall;
	}

	public void setRainfall(double rainfall) {
		this.rainfall = rainfall;
	}

	public double getEvaporation() {
		return evaporation;
	}

	public void setEvaporation(double evaporation) {
		this.evaporation = evaporation;
	}

	public double getSunshineNumber() {
		return sunshineNumber;
	}

	public void setSunshineNumber(double sunshineNumber) {
		this.sunshineNumber = sunshineNumber;
	}

	public double getRadiation() {
		return radiation;
	}

	public void setRadiation(double radiation) {
		this.radiation = radiation;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	
}
