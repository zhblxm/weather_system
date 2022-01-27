package com.partners.entity;
// Generated May 30, 2017 9:47:29 PM by Hibernate Tools 3.5.0.Final

import java.sql.Timestamp;

public class Terminalhistory extends BaseEntity implements java.io.Serializable {

	private static final long serialVersionUID = 6151351558539595352L;
	private int terminalHistoryId;
	private int terminalCatetoryId;
	private int weatherStationId;
	private String detail;
	private int invalid;
	private String source;
	private String extendInfo;
	private Timestamp createUTCDate;
	private String tableName;
	
	public int getTerminalHistoryId() {
		return this.terminalHistoryId;
	}

	public void setTerminalHistoryId(int terminalHistoryId) {
		this.terminalHistoryId = terminalHistoryId;
	}

	public String getDetail() {
		return this.detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getInvalid() {
		return this.invalid;
	}

	public void setInvalid(int invalid) {
		this.invalid = invalid;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getExtendInfo() {
		return this.extendInfo;
	}

	public void setExtendInfo(String extendInfo) {
		this.extendInfo = extendInfo;
	}

	public Timestamp getCreateUTCDate() {
		return createUTCDate;
	}

	public void setCreateUTCDate(Timestamp createUTCDate) {
		this.createUTCDate = createUTCDate;
	}

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

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
