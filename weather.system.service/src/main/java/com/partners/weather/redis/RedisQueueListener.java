package com.partners.weather.redis;

public interface RedisQueueListener<T> {  
	  
    public void onMessage(T value);  
}  
