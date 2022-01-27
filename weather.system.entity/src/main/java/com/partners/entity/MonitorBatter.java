package com.partners.entity;

import org.joda.time.DateTime;

public class MonitorBatter implements java.io.Serializable {
	private static final long serialVersionUID = 7507347152048353344L;
	private int terminalCagegoryId;
	private double batterValue;
	private int stationId;
	private DateTime lastUpdateTime;
	private double batterPercent;
	private boolean invalid;
	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTerminalCagegoryId() {
		return terminalCagegoryId;
	}

	public void setTerminalCagegoryId(int terminalCagegoryId) {
		this.terminalCagegoryId = terminalCagegoryId;
	}

	public double getBatterValue() {
		return batterValue;
	}

	public void setBatterValue(double batterValue) {
		this.batterValue = batterValue;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public DateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(DateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public double getBatterPercent() {
		return batterPercent;
	}

	public void setBatterPercent(double batterPercent) {
		this.batterPercent = batterPercent;
	}

}
