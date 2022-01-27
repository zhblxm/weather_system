package com.partners.weather.common;

public enum NotificationType {
	BatterAlert(1,"电量低"),	InvalidAlert(2,"无效数据"),	DataMissAlert(3,"长时间为收到数据"),ErrorStationAlert(4,"错误站点");
	private int value = 0;
	private String description="";
	NotificationType(int value,String description) {
		this.value = value;
		this.description=description;
	}
	public int getValue() {
		return value;
	}	
	public String getDescription() {
		return description;
	}

}
