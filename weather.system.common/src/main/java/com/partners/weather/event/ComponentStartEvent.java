package com.partners.weather.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import com.partners.weather.event.args.ComponentStartEventArgs;
import com.partners.weather.event.listener.ComponentStartEventListener;

public class ComponentStartEvent extends EventObject{
	private ComponentStartEventArgs args;
	private static List<ComponentStartEventListener> listenerList = new ArrayList<ComponentStartEventListener>();
	
	public ComponentStartEvent(Object source) {
		super(source);
		this.args = (ComponentStartEventArgs)source;
	}
	
	public ComponentStartEventArgs getArgs(){
		return this.args;
	}
	
	public void dispatchEvent()
    {
		for(int num=0; num<listenerList.size(); num++){
			listenerList.get(num).dispatchEvent(this);
		}
    }
	
	public static void addListner(ComponentStartEventListener listener)
    {
		listenerList.add(listener);
    }

    public static void removeListner(ComponentStartEventListener listener)
    {
    	listenerList.remove(listener);
    }
}
