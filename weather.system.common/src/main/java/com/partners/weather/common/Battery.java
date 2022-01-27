package com.partners.weather.common;

public enum Battery {
	directcurrent(0,"城市-直流电"),leadcell(1,"自动站-铅酸电池"),lithium(2,"便携站-锂电池");
	private final int id;
	private final String name;
	Battery(int id,String name) {
		this.id = id;
		this.name=name;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
}

