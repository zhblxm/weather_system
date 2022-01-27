package com.partners.weather.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.partners.weather.service.IParseReqeustService;

public class QueueListener<RequestMessage> implements RedisQueueListener<RequestMessage> {
	private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);
	
	@Autowired
	private IParseReqeustService parseReqeustService;
 
	@Override
	public void onMessage(RequestMessage value) {
		try {
			parseReqeustService.parse((com.partners.entity.RequestMessage)value);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}

}