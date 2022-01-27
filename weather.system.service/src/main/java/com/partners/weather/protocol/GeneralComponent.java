package com.partners.weather.protocol;

import com.partners.weather.common.ResultCode;

public abstract class GeneralComponent {
	protected int componentId;
	protected ComponentStatus status;
	private int port;
	
	public GeneralComponent(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public abstract ResultCode start();

	public abstract ResultCode stop();

	public int getComponentId() {
		return componentId;
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	public ComponentStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentStatus status) {
		this.status = status;
	}

	public abstract ResultCode pause();
}
