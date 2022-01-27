package com.partners.weather.protocol;

public class ComponentThread implements Runnable{
	private GeneralComponent component;
	
	public ComponentThread(GeneralComponent component){
		this.component = component;
	}

	@Override
	public void run() {
		this.component.start();
	}
}
