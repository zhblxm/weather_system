package com.partners.entity;

import java.io.Serializable;
import java.util.List;

public class City implements Serializable{

	private static final long serialVersionUID = 1L;

	public String code;

	public String name;

	public String rings;
	
	public List<Piecearea> Piecearea;

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

	public List<Piecearea> getPiecearea() {
		return Piecearea;
	}

	public void setPiecearea(List<Piecearea> piecearea) {
		Piecearea = piecearea;
	}

}
