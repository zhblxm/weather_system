package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Statistics;
import com.partners.view.entity.VStatistics;

public interface IWeatherStatisticsService {
	List<Statistics> getStatistics(VStatistics statistics);
}
