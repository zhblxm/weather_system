package com.partners.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;

@Data
@Builder
public class JsonResult implements Serializable {

	@Tolerate
	public JsonResult(){}
	private static final long serialVersionUID = 1L;
	private int draw;
	private int recordsTotal;
	private int recordsFiltered;
	private Object[] data;
	
}
