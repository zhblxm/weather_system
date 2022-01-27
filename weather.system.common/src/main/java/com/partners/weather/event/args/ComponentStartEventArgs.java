package com.partners.weather.event.args;

import com.partners.weather.common.ResultCode;

public class ComponentStartEventArgs {
	private ResultCode resultCode;
	private int componentId;
	
	public ComponentStartEventArgs(int componentId, ResultCode resultCode){
		this.resultCode = resultCode;
		this.componentId = componentId;
	}
	
	public ResultCode getResultCode(){
		return this.resultCode;
	}
	
	public int getComponentId(){
		return this.componentId;
	}
}
