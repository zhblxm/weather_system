package com.partners.view.entity;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.partners.entity.BaseEntity;

public class VTerminalhistory extends BaseEntity implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3010786299681542087L;
	private int terminalCatetoryId;
	private int weatherStationId;
	private String createDateFrom;
	private String createDateTo;
	private String tableName;

	public int getTerminalCatetoryId() {
		return terminalCatetoryId;
	}

	public void setTerminalCatetoryId(int terminalCatetoryId) {
		this.terminalCatetoryId = terminalCatetoryId;
	}

	public int getWeatherStationId() {
		return weatherStationId;
	}

	public void setWeatherStationId(int weatherStationId) {
		this.weatherStationId = weatherStationId;
	}

	public String getCreateDateFrom() {
		return createDateFrom;
	}

	public void setCreateDateFrom(String createDateFrom) {
		this.createDateFrom = createDateFrom;
	}

	public String getCreateDateTo() {
		return createDateTo;
	}

	public void setCreateDateTo(String createDateTo) {
		this.createDateTo = createDateTo;
	}

	public String getTableName() {
		if (StringUtils.isBlank(tableName)) {
			DateTime now;
			if (StringUtils.isBlank(createDateFrom)) {
				now = DateTime.now();
			} else {
				DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
				now = DateTime.parse(createDateFrom, format);
			}
			tableName = String.format("terminalhistory_%d_%d_%d_%d", terminalCatetoryId, weatherStationId,
					now.getYear(), now.getMonthOfYear());
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
