package com.partners.view.entity;

import java.sql.Timestamp;

public class vBaseEntity {

	public vBaseEntity()
	{
	}
	public vBaseEntity(vBaseEntity entity) {
		this(entity.getCreateDate(), entity.getName(), entity.getId(), entity.getOrderField(), entity.getOrderType(), entity.getStartIndex(), entity.getSize());
	}
	public vBaseEntity(Timestamp createDate, String name, int id, String orderField, String orderType, int startIndex, int size) {
		super();
		this.createDate = createDate;
		this.name = name;
		this.id = id;
		this.orderField = orderField;
		this.orderType = orderType;
		this.startIndex = startIndex;
		this.size = size;
	}
	private Timestamp createDate;
	private String name;
	private int id;
	private String orderField;
	private String orderType;
	private int startIndex;
	private int size;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public String getOrderField() {
		return orderField;
	}
	public void setOrderField(String orderField) {
		this.orderField = orderField;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
