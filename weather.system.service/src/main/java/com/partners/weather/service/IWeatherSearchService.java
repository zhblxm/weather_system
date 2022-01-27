package com.partners.weather.service;

import java.util.List;
import java.util.Map;

import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.WeatherStationTerminal;
import com.partners.view.entity.VStationSearch;

public interface IWeatherSearchService {
	List<String> getWeathers(VStationSearch stationSearch,Map<String, TerminalParametersAttrs> parameterMap,List<WeatherStationTerminal> terminals);
	
	String getWeathersByLineChart(VStationSearch stationSearch, TerminalParametersAttrs terminalParameters, int lineChartType);
}
