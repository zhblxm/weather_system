package com.partners.entity;

import java.io.Serializable;

public class Piecearea implements Serializable {

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

	public String getRings() {
		return rings;
	}

	public void setRings(String rings) {
		this.rings = rings;
	}

	private static final long serialVersionUID = -5805461152140039376L;

	public String code;

	public String name;

	public String rings;

}
