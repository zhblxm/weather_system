package com.partners.weather.common;

import org.joda.time.DateTime;

public class CommonResources {
	public static final String[] DATETYPE = new String[] { "string", "double", "date", "time", "datetime","integer" };
	public static final String[] SYSTEM_DATETYPE = new String[] { "date", "time", "datetime" };
	public static final String[] DATEFORMATE = new String[] { "yyyy-MM-dd", "HH:mm:ss", "yyyyMMddHHmmss" };
	public static final String ADMINUSERKEY = "ADMINUSERKEY";
	public static final String WEATHERSYSTEM = "weathersystem";
	public static final String WEATHERSYSTEMALIAS = "weathersystemalias";
	public static final String WEATHERSYSTEMINVALID = "weathersysteminvalid";
	public static final String WEATHERSYSTEMINVALIDALIAS = "weathersysteminvalidalias";
	public static final String STATIONINVALID = "stationinvalid";
	public static final String STATIONINVALIDALIAS = "stationinvalidalias";
	public static final String INVALIDSTATIONTYPE = "InvalidStation";
	public static final String HEARTBEAT = "Heartbeat";
	public static final String COUNTRY = "Country";
	public static final String NEWLINE = "<NEWLINE>";
	public static final String AUTOREFRESHNUMBER = "AUTOREFRESHNUMBER";
	public static final String AUTOREFRESHDATE = "AUTOREFRESHDATE";
	public static final String AUTOREFRESHDATETYPE = "AUTOREFRESHDATETYPE";
	public static final String TERMINALDATETASK = "TERMINALDATETASK";
	public static final String DATAACQUISITIONTASK = "DATAACQUISITIONTASK";
	public static final String MONITORBATTERY = "MONITORBATTERY";
	public static final String SERIALPORT = "SERIALPORT";
	public static final String BAUDRATE = "BAUDRATE";
	public static final int PageSize = 20;
	public static final int extParametersSize = 8;
	public static String MapImageHost = "";
	public static final DateTime SYSTEMLAUNCHDATE = new DateTime();
	public static final int DELAYEDCOUNT = 5;
	public static int ONLINECOUNT=30;
	public static int ONOROFFLINECOUNT = 60;
	public static final String NOTIFICATIONS = "NOTIFICATIONS";
	public static final String MONITORTASK = "MONITORTASK";
	public static final String IMAGEMONITORPATH = "IMAGEMONITORPATH";
	public static final String IMAGEMONITORTASK = "IMAGEMONITORTASK";
	public static final String FILEPUSHFTPURL = "FILEPUSHFTPURL";
	public static final String FILEPUSHFTPPORT = "FILEPUSHFTPPORT";
	public static final String FILEPUSHFTPUSER = "FILEPUSHFTPUSER";
	public static final String FILEPUSHFTPPWD = "FILEPUSHFTPPWD";
	public static final String FILEPUSHFTPTYPE = "FILEPUSHFTPTYPE";
	public static final String FILEPUSHONOFF = "FILEPUSHONOFF";
}
