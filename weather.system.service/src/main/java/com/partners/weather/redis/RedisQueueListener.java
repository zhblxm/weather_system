package com.partners.weather.redis;

public interface RedisQueueListener<T> {  
	  
    void onMessage(T value);
}  
