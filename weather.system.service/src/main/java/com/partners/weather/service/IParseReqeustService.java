package com.partners.weather.service;

import com.partners.entity.RequestMessage;

public interface IParseReqeustService {
	void parse(RequestMessage value) throws Exception;
}
