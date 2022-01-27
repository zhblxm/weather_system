package com.partners.weather.service;

import java.util.List;
import java.util.Map;

import com.partners.entity.AggregationEntity;
import com.partners.entity.WeatherStationTerminal;
import com.partners.view.entity.VAgg;

public interface IAggregationService {
	 List<AggregationEntity> getAggregationRecords(VAgg vagg,	List<WeatherStationTerminal> terminals,Map<String, String> paramMap);
	 double getAggRainfall(VAgg vagg,	WeatherStationTerminal terminal);
}