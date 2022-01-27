package com.partners.entity;


import java.io.Serializable;
import java.util.List;

public class Country implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public String code;
	public String name;
	public List<Province> Province;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Province> getProvince() {
		return Province;
	}

	public void setProvince(List<Province> province) {
		Province = province;
	}

}
