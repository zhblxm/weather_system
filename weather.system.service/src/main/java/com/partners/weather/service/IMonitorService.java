package com.partners.weather.service;

import java.util.ArrayList;
import java.util.Map;

import com.partners.entity.WeatherStationTerminal;

public interface IMonitorService {
    Map<String, String> getLastWeatherDetail(int stationId, ArrayList<WeatherStationTerminal> weatherStationTerminals);

    long getLastHourWeathers();
}
