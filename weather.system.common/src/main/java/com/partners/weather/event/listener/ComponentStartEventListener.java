package com.partners.weather.event.listener;

import java.util.EventListener;

import com.partners.weather.event.ComponentStartEvent;

public interface ComponentStartEventListener extends EventListener{
	public void dispatchEvent(ComponentStartEvent event);
}
