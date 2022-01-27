package com.partners.weather.event.listener;

import com.partners.weather.event.ComponentStartEvent;

public class ComponentStartListener implements ComponentStartEventListener{

	private ComponentStartEvent event = null;
	public ComponentStartListener(){
	}
	
	public ComponentStartEvent getEvent(){
		return this.event;
	}
	
	@Override
	public void dispatchEvent(ComponentStartEvent event) {
		this.event = event;
		ComponentStartEvent.removeListner(this);
	}

}
