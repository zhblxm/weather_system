package com.partners.weather.redis;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.partners.weather.service.IParseReqeustService;

@Slf4j
public class QueueListener<RequestMessage> implements RedisQueueListener<RequestMessage> {

    @Autowired
    private IParseReqeustService parseReqeustService;

    @Override
    public void onMessage(RequestMessage value) {
        try {
            parseReqeustService.parse((com.partners.entity.RequestMessage) value);
        } catch (Exception exception) {
            log.error("Error in {}", exception);
        }
    }

}