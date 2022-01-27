package com.partners.entity;


import java.io.Serializable;
import java.util.List;

public class Province implements Serializable {

	private static final long serialVersionUID = -252907436718521533L;
	public String id;
	public String code;
	public String name;
	public String rings;
	public List<City> city;

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

	public List<City> getCity() {
		return city;
	}

	public void setCity(List<City> city) {
		this.city = city;
	}

	public String getRings() {
		return rings;
	}

	public void setRings(String rings) {
		this.rings = rings;
	}

}
